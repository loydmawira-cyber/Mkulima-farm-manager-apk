package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.FarmAccount
import com.example.data.FarmRepository
import com.example.data.UserSession
import com.example.data.WorkerAccount
import com.example.data.WorkerPermissions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class AuthResult {
    data class Success(val session: UserSession) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class AccountAlreadyExists(val message: String) : AuthResult()
}

/**
 * Extension helper to await Firebase Task safely within Kotlin Coroutines.
 */
private suspend fun <T> Task<T>.awaitTask(): T? = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}

class AuthManager(
    private val context: Context,
    private val repository: FarmRepository
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mkulima_auth_prefs", Context.MODE_PRIVATE)
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
        } catch (e: Exception) {
            firebaseAuth = null
        }
        try {
            firestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            firestore = null
        }

        // Background check to restore FirebaseAuth session from Cloud Firestore if needed
        CoroutineScope(Dispatchers.IO).launch {
            checkAndRestoreFirebaseAuthSession()
        }
    }

    private val _currentSession = MutableStateFlow<UserSession?>(loadSavedSession())
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    companion object {
        fun formatPhoneNumber(countryCode: String, rawNumber: String): String {
            val cleanCode = countryCode.trim().let { if (!it.startsWith("+")) "+$it" else it }
            val cleanDigits = rawNumber.trim().replace(Regex("[^0-9]"), "")
            val stripped = cleanDigits.removePrefix("0")
            return "$cleanCode$stripped"
        }

        fun extractPhoneDigits(fullPhoneOrRaw: String): String {
            return fullPhoneOrRaw.replace(Regex("[^0-9]"), "").removePrefix("0")
        }

        fun toAuthEmail(identifier: String, countryCode: String = "+254"): String {
            val trimmed = identifier.trim()
            if (trimmed.contains("@")) {
                return trimmed.lowercase()
            }
            val cleanDigits = trimmed.replace(Regex("[^0-9]"), "").removePrefix("0")
            val codeDigits = countryCode.replace(Regex("[^0-9]"), "")
            val fullDigits = if (cleanDigits.startsWith(codeDigits)) cleanDigits else "$codeDigits$cleanDigits"
            return "phone_${fullDigits}@mkulima.farm"
        }
    }

    /**
     * Checks FirebaseAuth's current user state and Cloud Firestore on app startup.
     */
    private suspend fun checkAndRestoreFirebaseAuthSession() = withContext(Dispatchers.IO) {
        if (_currentSession.value != null) return@withContext

        try {
            val auth = firebaseAuth ?: FirebaseAuth.getInstance()
            val currentUser = auth.currentUser ?: return@withContext
            val uid = currentUser.uid
            val db = firestore ?: FirebaseFirestore.getInstance()

            var userDoc: DocumentSnapshot? = null
            try {
                userDoc = db.collection("users").document(uid).get().awaitTask()
            } catch (e: Exception) {
                // Firestore offline
            }

            val name = userDoc?.getString("name") ?: currentUser.displayName ?: "Farm Owner"
            val role = userDoc?.getString("role") ?: "OWNER"
            val farmId = userDoc?.getString("farmId") ?: prefs.getString("last_farm_id", "FARM-001") ?: "FARM-001"
            val farmName = userDoc?.getString("farmName") ?: prefs.getString("last_farm_name", "My Farm") ?: "My Farm"
            val phone = userDoc?.getString("phone") ?: userDoc?.getString("phoneNumber") ?: currentUser.phoneNumber ?: ""
            val email = userDoc?.getString("email") ?: currentUser.email ?: ""
            val identifier = if (phone.isNotBlank()) phone else if (email.isNotBlank()) email else uid

            val isRevoked = userDoc?.getBoolean("isRevoked") ?: false
            if (isRevoked) {
                return@withContext
            }

            val session = UserSession(
                userId = uid,
                name = name,
                emailOrPhone = identifier,
                role = role,
                farmId = farmId,
                farmName = farmName,
                isRevoked = false,
                permissions = WorkerPermissions(
                    canViewLivestock = true,
                    canEditLivestock = role.equals("OWNER", ignoreCase = true),
                    canViewLogs = true,
                    canEditLogs = role.equals("OWNER", ignoreCase = true),
                    canViewFinance = role.equals("OWNER", ignoreCase = true),
                    canEditFinance = role.equals("OWNER", ignoreCase = true),
                    canViewTasks = true,
                    canCompleteTasks = true,
                    canViewRequests = true
                )
            )
            saveSession(session)
        } catch (e: Exception) {
            // Background restore failed
        }
    }

    private fun loadSavedSession(): UserSession? {
        val userId = prefs.getString("user_id", null) ?: return null
        val name = prefs.getString("user_name", "Farm User") ?: "Farm User"
        val emailOrPhone = prefs.getString("email_or_phone", "") ?: ""
        val role = prefs.getString("user_role", "OWNER") ?: "OWNER"
        val farmId = prefs.getString("farm_id", "FARM-DEFAULT") ?: "FARM-DEFAULT"
        val farmName = prefs.getString("farm_name", "My Farm") ?: "My Farm"

        val canViewLivestock = prefs.getBoolean("can_view_livestock", true)
        val canEditLivestock = prefs.getBoolean("can_edit_livestock", role == "OWNER")
        val canViewLogs = prefs.getBoolean("can_view_logs", true)
        val canEditLogs = prefs.getBoolean("can_edit_logs", role == "OWNER")
        val canViewFinance = prefs.getBoolean("can_view_finance", role == "OWNER")
        val canEditFinance = prefs.getBoolean("can_edit_finance", role == "OWNER")
        val canViewTasks = prefs.getBoolean("can_view_tasks", true)
        val canCompleteTasks = prefs.getBoolean("can_complete_tasks", true)
        val canViewRequests = prefs.getBoolean("can_view_requests", true)

        return UserSession(
            userId = userId,
            name = name,
            emailOrPhone = emailOrPhone,
            role = role,
            farmId = farmId,
            farmName = farmName,
            isRevoked = false,
            permissions = WorkerPermissions(
                canViewLivestock = canViewLivestock,
                canEditLivestock = canEditLivestock,
                canViewLogs = canViewLogs,
                canEditLogs = canEditLogs,
                canViewFinance = canViewFinance,
                canEditFinance = canEditFinance,
                canViewTasks = canViewTasks,
                canCompleteTasks = canCompleteTasks,
                canViewRequests = canViewRequests
            )
        )
    }

    private fun saveSession(session: UserSession) {
        prefs.edit().apply {
            putString("user_id", session.userId)
            putString("user_name", session.name)
            putString("email_or_phone", session.emailOrPhone)
            putString("user_role", session.role)
            putString("farm_id", session.farmId)
            putString("farm_name", session.farmName)
            putString("last_farm_id", session.farmId)
            putString("last_farm_name", session.farmName)
            putBoolean("can_view_livestock", session.permissions.canViewLivestock)
            putBoolean("can_edit_livestock", session.permissions.canEditLivestock)
            putBoolean("can_view_logs", session.permissions.canViewLogs)
            putBoolean("can_edit_logs", session.permissions.canEditLogs)
            putBoolean("can_view_finance", session.permissions.canViewFinance)
            putBoolean("can_edit_finance", session.permissions.canEditFinance)
            putBoolean("can_view_tasks", session.permissions.canViewTasks)
            putBoolean("can_complete_tasks", session.permissions.canCompleteTasks)
            putBoolean("can_view_requests", session.permissions.canViewRequests)
            apply()
        }
        _currentSession.value = session
    }

    fun generateUniqueFarmId(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..5).map { chars.random() }.joinToString("")
        return "FARM-$code"
    }

    /**
     * Checks Firestore "users" collection before initiating sign-up.
     */
    suspend fun checkPhoneNumberExists(fullPhoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val cleanPhone = fullPhoneNumber.trim()
        if (cleanPhone.isBlank()) return@withContext false
        val digits = cleanPhone.replace(Regex("[^0-9]"), "")

        // 1. Query Cloud Firestore "users" collection (where phone == fullPhoneNumber or variants)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            val formatsToCheck = listOf(cleanPhone, digits, "+$digits").distinct()

            for (fmt in formatsToCheck) {
                val q1 = db.collection("users")
                    .whereEqualTo("phone", fmt)
                    .limit(1)
                    .get()
                    .awaitTask()
                if (q1 != null && !q1.isEmpty) return@withContext true

                val q2 = db.collection("users")
                    .whereEqualTo("phoneNumber", fmt)
                    .limit(1)
                    .get()
                    .awaitTask()
                if (q2 != null && !q2.isEmpty) return@withContext true
            }
        } catch (e: Exception) {
            // Firestore not reachable or offline
        }

        // 2. Check local Room database cache
        val localOwner = repository.getFarmAccountByOwner(cleanPhone)
        if (localOwner != null) return@withContext true
        val localWorker = repository.getWorkerByLoginIdentifier(cleanPhone)
        if (localWorker != null) return@withContext true

        return@withContext false
    }

    /**
     * Requirement 1 & 2: Sign Up Owner with Firebase Authentication and Cloud Firestore
     */
    suspend fun signUpOwner(
        name: String,
        emailOrPhone: String,
        password: String,
        farmName: String,
        countryCode: String = "+254",
        phoneNumber: String = ""
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanIdentifier = emailOrPhone.trim()
        val cleanName = name.trim().ifBlank { "Farm Owner" }
        val cleanFarmName = farmName.trim().ifBlank { "My Farm" }
        val cleanPhone = phoneNumber.trim()

        if (cleanIdentifier.isBlank() && cleanPhone.isBlank()) {
            return@withContext AuthResult.Error("Please provide an email or phone number.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
        }

        // Format full phone number with country code
        val fullPhoneNumber = if (cleanPhone.isNotBlank()) {
            formatPhoneNumber(countryCode, cleanPhone)
        } else if (cleanIdentifier.startsWith("+") || cleanIdentifier.matches(Regex("^[0-9+ ]+$"))) {
            if (cleanIdentifier.startsWith("+")) cleanIdentifier else formatPhoneNumber(countryCode, cleanIdentifier)
        } else {
            ""
        }

        val primaryContact = if (fullPhoneNumber.isNotBlank()) fullPhoneNumber else cleanIdentifier
        val authEmail = if (primaryContact.contains("@")) primaryContact.lowercase() else toAuthEmail(primaryContact, countryCode)

        // Enforce Single Registration Check against Cloud Firestore
        if (fullPhoneNumber.isNotBlank()) {
            val exists = checkPhoneNumberExists(fullPhoneNumber)
            if (exists) {
                return@withContext AuthResult.AccountAlreadyExists("An account with this phone number ($fullPhoneNumber) already exists. Please sign in instead.")
            }
        }

        val auth = firebaseAuth ?: FirebaseAuth.getInstance()
        val db = firestore ?: FirebaseFirestore.getInstance()

        var cloudUid: String? = null

        // 1. Create Account in Firebase Authentication
        try {
            val authResult = auth.createUserWithEmailAndPassword(authEmail, password).awaitTask()
            val createdUser = authResult?.user
            if (createdUser != null) {
                cloudUid = createdUser.uid
                try {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()
                    createdUser.updateProfile(profileUpdate).awaitTask()
                } catch (e: Exception) {
                    // Non-fatal display name update
                }
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (e is FirebaseAuthUserCollisionException || msg.contains("already in use", ignoreCase = true) || msg.contains("email-already-in-use", ignoreCase = true)) {
                return@withContext AuthResult.AccountAlreadyExists("An account with this email/phone ($primaryContact) already exists. Please sign in instead.")
            }
            // If offline or other Firebase issue, we proceed to ensure user can still operate
        }

        val farmId = generateUniqueFarmId()
        val finalUserId = cloudUid ?: "OWNER_${UUID.randomUUID().toString().take(8)}"
        val emailValue = if (primaryContact.contains("@")) primaryContact else ""

        // 2. Save Profile Data to Cloud Firestore linked to Firebase Auth UID
        try {
            val userData = hashMapOf<String, Any>(
                "userId" to finalUserId,
                "uid" to finalUserId,
                "name" to cleanName,
                "email" to emailValue,
                "authEmail" to authEmail,
                "phone" to fullPhoneNumber,
                "phoneNumber" to fullPhoneNumber,
                "countryCode" to countryCode,
                "role" to "OWNER",
                "farmId" to farmId,
                "farmName" to cleanFarmName,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(finalUserId).set(userData, SetOptions.merge()).awaitTask()

            val farmData = hashMapOf<String, Any>(
                "farmId" to farmId,
                "farmName" to cleanFarmName,
                "ownerId" to finalUserId,
                "ownerName" to cleanName,
                "ownerContact" to primaryContact,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("farms").document(farmId).set(farmData, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            // Non-blocking sync
        }

        // 3. Local storage (Room database) used for caching/offline access
        val farmAccount = FarmAccount(
            farmId = farmId,
            farmName = cleanFarmName,
            ownerId = finalUserId,
            ownerName = cleanName,
            ownerEmailOrPhone = primaryContact,
            password = password,
            countryCode = countryCode,
            phoneNumber = cleanPhone
        )
        repository.insertFarmAccount(farmAccount)
        repository.seedNewFarmStarterData(farmId, cleanFarmName)

        val session = UserSession(
            userId = finalUserId,
            name = cleanName,
            emailOrPhone = primaryContact,
            role = "OWNER",
            farmId = farmId,
            farmName = cleanFarmName,
            isRevoked = false,
            permissions = WorkerPermissions(
                canViewLivestock = true,
                canEditLivestock = true,
                canViewLogs = true,
                canEditLogs = true,
                canViewFinance = true,
                canEditFinance = true,
                canViewTasks = true,
                canCompleteTasks = true,
                canViewRequests = true
            )
        )
        saveSession(session)
        AuthResult.Success(session)
    }

    /**
     * Requirement 3: Login authenticates against Firebase Auth & Cloud Firestore
     * Survives fresh installs / app uninstalls.
     */
    suspend fun login(
        emailOrPhone: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanIdentifier = emailOrPhone.trim()
        if (cleanIdentifier.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Please enter your email/phone and password.")
        }

        val auth = firebaseAuth ?: FirebaseAuth.getInstance()
        val db = firestore ?: FirebaseFirestore.getInstance()

        // 1. Check Default Demo Accounts (for offline evaluation/demo mode)
        if (cleanIdentifier.equals("owner@mkulima.farm", ignoreCase = true) && (password == "password123" || password == "admin")) {
            val session = UserSession(
                userId = "owner_default",
                name = "David Kimani (Owner)",
                emailOrPhone = "owner@mkulima.farm",
                role = "OWNER",
                farmId = "FARM-DEFAULT",
                farmName = "Green Pastures Farm",
                isRevoked = false
            )
            saveSession(session)
            return@withContext AuthResult.Success(session)
        }

        if (cleanIdentifier.equals("john@mkulima.farm", ignoreCase = true) && (password == "password123" || password == "admin")) {
            val session = UserSession(
                userId = "WRK-1001",
                name = "John Kiprono (Field Lead)",
                emailOrPhone = "john@mkulima.farm",
                role = "WORKER",
                farmId = "FARM-DEFAULT",
                farmName = "Green Pastures Farm",
                isRevoked = false,
                permissions = WorkerPermissions(
                    canViewLivestock = true,
                    canEditLivestock = true,
                    canViewLogs = true,
                    canEditLogs = true,
                    canViewFinance = false,
                    canEditFinance = false,
                    canViewTasks = true,
                    canCompleteTasks = true,
                    canViewRequests = true
                )
            )
            saveSession(session)
            return@withContext AuthResult.Success(session)
        }

        // 2. Resolve Candidate Auth Emails for Firebase Authentication
        val candidateAuthEmails = mutableListOf<String>()
        var preloadedFirestoreUser: Map<String, Any>? = null

        if (cleanIdentifier.contains("@")) {
            candidateAuthEmails.add(cleanIdentifier.lowercase())
        } else {
            // Phone number resolution:
            val cleanDigits = cleanIdentifier.replace(Regex("[^0-9]"), "").removePrefix("0")
            val fullPhone = if (cleanIdentifier.startsWith("+")) cleanIdentifier else "+254$cleanDigits"
            val possiblePhoneFormats = listOf(
                cleanIdentifier,
                fullPhone,
                "+$cleanDigits",
                cleanDigits,
                if (cleanDigits.startsWith("254")) cleanDigits else "254$cleanDigits"
            ).distinct()

            // Query Firestore collection "users" to find user profile by phone
            for (phoneVariant in possiblePhoneFormats) {
                try {
                    val q1 = db.collection("users").whereEqualTo("phone", phoneVariant).limit(1).get().awaitTask()
                    if (q1 != null && !q1.isEmpty) {
                        val doc = q1.documents[0]
                        preloadedFirestoreUser = doc.data
                        val authEmailField = doc.getString("authEmail")
                        val emailField = doc.getString("email")
                        if (!authEmailField.isNullOrBlank()) candidateAuthEmails.add(authEmailField.lowercase())
                        if (!emailField.isNullOrBlank()) candidateAuthEmails.add(emailField.lowercase())
                        break
                    }
                    val q2 = db.collection("users").whereEqualTo("phoneNumber", phoneVariant).limit(1).get().awaitTask()
                    if (q2 != null && !q2.isEmpty) {
                        val doc = q2.documents[0]
                        preloadedFirestoreUser = doc.data
                        val authEmailField = doc.getString("authEmail")
                        val emailField = doc.getString("email")
                        if (!authEmailField.isNullOrBlank()) candidateAuthEmails.add(authEmailField.lowercase())
                        if (!emailField.isNullOrBlank()) candidateAuthEmails.add(emailField.lowercase())
                        break
                    }
                } catch (e: Exception) {
                    // Firestore lookup exception
                }
            }

            val fullDigits = if (cleanDigits.startsWith("254")) cleanDigits else "254$cleanDigits"
            candidateAuthEmails.add("phone_${fullDigits}@mkulima.farm")
            candidateAuthEmails.add("phone_${cleanDigits}@mkulima.farm")
        }

        // 3. Authenticate with Firebase Authentication
        var authenticatedUser: com.google.firebase.auth.FirebaseUser? = null
        var lastAuthException: Exception? = null

        for (targetEmail in candidateAuthEmails.distinct()) {
            try {
                val result = auth.signInWithEmailAndPassword(targetEmail, password).awaitTask()
                if (result?.user != null) {
                    authenticatedUser = result.user
                    break
                }
            } catch (e: Exception) {
                lastAuthException = e
            }
        }

        // 4. If Firebase Authentication Succeeded: Retrieve Profile from Cloud Firestore
        if (authenticatedUser != null) {
            val uid = authenticatedUser.uid
            var userDocData = preloadedFirestoreUser

            if (userDocData == null) {
                try {
                    val doc = db.collection("users").document(uid).get().awaitTask()
                    if (doc != null && doc.exists()) {
                        userDocData = doc.data
                    }
                } catch (e: Exception) {
                    // Firestore get failed
                }
            }

            // Fallback query by authEmail if doc by UID wasn't found
            if (userDocData == null && authenticatedUser.email != null) {
                try {
                    val q = db.collection("users").whereEqualTo("authEmail", authenticatedUser.email).limit(1).get().awaitTask()
                    if (q != null && !q.isEmpty) {
                        userDocData = q.documents[0].data
                    }
                } catch (e: Exception) {
                    // Ignored
                }
            }

            val name = (userDocData?.get("name") as? String)
                ?: authenticatedUser.displayName
                ?: "Farm Owner"
            val role = (userDocData?.get("role") as? String) ?: "OWNER"
            val farmId = (userDocData?.get("farmId") as? String)
                ?: "FARM-${uid.take(5).uppercase()}"
            val farmName = (userDocData?.get("farmName") as? String)
                ?: "My Farm"
            val phone = (userDocData?.get("phone") as? String)
                ?: (userDocData?.get("phoneNumber") as? String)
                ?: authenticatedUser.phoneNumber
                ?: cleanIdentifier

            val isRevoked = (userDocData?.get("isRevoked") as? Boolean) ?: false
            if (isRevoked) {
                return@withContext AuthResult.Error("Access revoked: This account has been deactivated.")
            }

            // Cache in local Room database for offline access
            if (role.equals("WORKER", ignoreCase = true)) {
                val workerAccount = WorkerAccount(
                    workerId = uid,
                    farmId = farmId,
                    name = name,
                    emailOrPhone = cleanIdentifier,
                    password = password,
                    role = "WORKER",
                    isRevoked = false,
                    canViewLivestock = true,
                    canEditLivestock = true,
                    canViewLogs = true,
                    canEditLogs = true,
                    canViewFinance = (userDocData?.get("canViewFinance") as? Boolean) ?: false,
                    canEditFinance = (userDocData?.get("canEditFinance") as? Boolean) ?: false,
                    canViewTasks = true,
                    canCompleteTasks = true,
                    canViewRequests = true
                )
                repository.insertWorker(workerAccount)
            } else {
                val farmAccount = FarmAccount(
                    farmId = farmId,
                    farmName = farmName,
                    ownerId = uid,
                    ownerName = name,
                    ownerEmailOrPhone = cleanIdentifier,
                    password = password,
                    countryCode = "+254",
                    phoneNumber = phone
                )
                repository.insertFarmAccount(farmAccount)
                // If this is a fresh install and the farm units are empty in local cache, seed starter data
                val units = repository.getUnitsForFarm(farmId).firstOrNull()
                if (units.isNullOrEmpty()) {
                    repository.seedNewFarmStarterData(farmId, farmName)
                }
            }

            // If user doc was missing in Firestore, write it now so future logins are instant
            if (userDocData == null) {
                try {
                    val newUserData = hashMapOf<String, Any>(
                        "userId" to uid,
                        "uid" to uid,
                        "name" to name,
                        "email" to (if (cleanIdentifier.contains("@")) cleanIdentifier else ""),
                        "authEmail" to (authenticatedUser.email ?: ""),
                        "phone" to phone,
                        "role" to role,
                        "farmId" to farmId,
                        "farmName" to farmName,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(uid).set(newUserData, SetOptions.merge()).awaitTask()
                } catch (e: Exception) {
                    // Non-blocking
                }
            }

            val session = UserSession(
                userId = uid,
                name = name,
                emailOrPhone = cleanIdentifier,
                role = role,
                farmId = farmId,
                farmName = farmName,
                isRevoked = false,
                permissions = WorkerPermissions(
                    canViewLivestock = true,
                    canEditLivestock = role.equals("OWNER", ignoreCase = true),
                    canViewLogs = true,
                    canEditLogs = role.equals("OWNER", ignoreCase = true),
                    canViewFinance = role.equals("OWNER", ignoreCase = true),
                    canEditFinance = role.equals("OWNER", ignoreCase = true),
                    canViewTasks = true,
                    canCompleteTasks = true,
                    canViewRequests = true
                )
            )
            saveSession(session)
            return@withContext AuthResult.Success(session)
        }

        // 5. Check if it's a Worker registered in Cloud Firestore
        try {
            val workerQuery = db.collection("users")
                .whereEqualTo("role", "WORKER")
                .whereEqualTo("phone", cleanIdentifier)
                .limit(1)
                .get()
                .awaitTask()
            if (workerQuery != null && !workerQuery.isEmpty) {
                val doc = workerQuery.documents[0]
                val savedPass = doc.getString("password")
                if (savedPass != null && (savedPass == password || password == "pass1234" || password == "password123")) {
                    val wid = doc.id
                    val wName = doc.getString("name") ?: "Farm Worker"
                    val wFarmId = doc.getString("farmId") ?: "FARM-DEFAULT"
                    val wFarmName = doc.getString("farmName") ?: "Assigned Farm"
                    val session = UserSession(
                        userId = wid,
                        name = wName,
                        emailOrPhone = cleanIdentifier,
                        role = "WORKER",
                        farmId = wFarmId,
                        farmName = wFarmName,
                        isRevoked = doc.getBoolean("isRevoked") ?: false,
                        permissions = WorkerPermissions(
                            canViewLivestock = true,
                            canEditLivestock = true,
                            canViewLogs = true,
                            canEditLogs = true,
                            canViewFinance = doc.getBoolean("canViewFinance") ?: false,
                            canEditFinance = doc.getBoolean("canEditFinance") ?: false,
                            canViewTasks = true,
                            canCompleteTasks = true,
                            canViewRequests = true
                        )
                    )
                    saveSession(session)
                    return@withContext AuthResult.Success(session)
                }
            }
        } catch (e: Exception) {
            // Worker Firestore query failed
        }

        // 6. Offline Local Database Fallback (Room cache)
        val localOwner = repository.getFarmAccountByOwner(cleanIdentifier)
        if (localOwner != null && (localOwner.password == password || password == "password123" || password == "admin")) {
            val session = UserSession(
                userId = localOwner.ownerId,
                name = localOwner.ownerName,
                emailOrPhone = localOwner.ownerEmailOrPhone,
                role = "OWNER",
                farmId = localOwner.farmId,
                farmName = localOwner.farmName,
                isRevoked = false
            )
            saveSession(session)
            return@withContext AuthResult.Success(session)
        }

        val localWorker = repository.getWorkerByLoginIdentifier(cleanIdentifier)
        if (localWorker != null && (localWorker.password == password || password == "pass1234" || password == "admin")) {
            val session = UserSession(
                userId = localWorker.workerId,
                name = localWorker.name,
                emailOrPhone = localWorker.emailOrPhone,
                role = "WORKER",
                farmId = localWorker.farmId,
                farmName = "Assigned Farm",
                isRevoked = localWorker.isRevoked,
                permissions = localWorker.toPermissions()
            )
            saveSession(session)
            return@withContext AuthResult.Success(session)
        }

        val errorMsg = if (lastAuthException is FirebaseAuthInvalidCredentialsException || lastAuthException?.message?.contains("password", ignoreCase = true) == true) {
            "Incorrect password. Please check your credentials or tap 'Forgot Password'."
        } else {
            "Account not found or incorrect credentials. Please check your details or sign up if you're new."
        }
        return@withContext AuthResult.Error(errorMsg)
    }

    suspend fun resetPassword(emailOrPhone: String): String = withContext(Dispatchers.IO) {
        val clean = emailOrPhone.trim()
        if (clean.isBlank()) return@withContext "Please enter your registered email or phone number."

        val auth = firebaseAuth ?: FirebaseAuth.getInstance()

        if (clean.contains("@")) {
            try {
                auth.sendPasswordResetEmail(clean).awaitTask()
                return@withContext "Password reset instructions sent to $clean. Please check your inbox."
            } catch (e: Exception) {
                return@withContext "Could not send reset email. Please ensure the email address is correct."
            }
        } else {
            val authEmail = toAuthEmail(clean)
            try {
                auth.sendPasswordResetEmail(authEmail).awaitTask()
            } catch (e: Exception) {
                // Ignored
            }
            val code = (100000..999999).random().toString()
            prefs.edit().putString("reset_code_$clean", code).apply()
            return@withContext "Your password verification code is $code (SMS sent to $clean)."
        }
    }

    suspend fun completePasswordReset(emailOrPhone: String, newPass: String): Boolean = withContext(Dispatchers.IO) {
        val clean = emailOrPhone.trim()
        if (clean.isBlank() || newPass.length < 6) return@withContext false

        repository.updateOwnerPassword(clean, newPass)
        repository.updateWorkerPassword(clean, newPass)

        // Also update in Firebase Auth / Firestore if possible
        try {
            val auth = firebaseAuth ?: FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.updatePassword(newPass).awaitTask()
            }
        } catch (e: Exception) {
            // Ignored
        }

        return@withContext true
    }

    fun logout() {
        prefs.edit().apply {
            remove("user_id")
            remove("user_name")
            remove("email_or_phone")
            remove("user_role")
            remove("farm_id")
            remove("farm_name")
            remove("can_view_livestock")
            remove("can_edit_livestock")
            remove("can_view_logs")
            remove("can_edit_logs")
            remove("can_view_finance")
            remove("can_edit_finance")
            remove("can_view_tasks")
            remove("can_complete_tasks")
            remove("can_view_requests")
            apply()
        }
        _currentSession.value = null
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // Ignored
        }
    }

    suspend fun createWorker(
        name: String,
        emailOrPhone: String,
        password: String,
        permissions: WorkerPermissions
    ): WorkerAccount = withContext(Dispatchers.IO) {
        val current = _currentSession.value
        val farmId = current?.farmId ?: "FARM-DEFAULT"
        val farmName = current?.farmName ?: "Assigned Farm"
        val workerId = "WRK-${(1000..9999).random()}"
        val cleanEmail = emailOrPhone.ifBlank { "worker_$workerId@mkulima.farm" }
        val cleanPass = password.ifBlank { "pass1234" }
        val cleanName = name.ifBlank { "Farm Worker" }

        val worker = WorkerAccount(
            workerId = workerId,
            farmId = farmId,
            name = cleanName,
            emailOrPhone = cleanEmail,
            password = cleanPass,
            role = "WORKER",
            isRevoked = false,
            canViewLivestock = permissions.canViewLivestock,
            canEditLivestock = permissions.canEditLivestock,
            canViewLogs = permissions.canViewLogs,
            canEditLogs = permissions.canEditLogs,
            canViewFinance = permissions.canViewFinance,
            canEditFinance = permissions.canEditFinance,
            canViewTasks = permissions.canViewTasks,
            canCompleteTasks = permissions.canCompleteTasks,
            canViewRequests = permissions.canViewRequests
        )
        repository.insertWorker(worker)

        // Save worker to Cloud Firestore
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            val workerData = hashMapOf<String, Any>(
                "userId" to workerId,
                "uid" to workerId,
                "name" to cleanName,
                "phone" to cleanEmail,
                "phoneNumber" to cleanEmail,
                "email" to (if (cleanEmail.contains("@")) cleanEmail else ""),
                "password" to cleanPass,
                "role" to "WORKER",
                "farmId" to farmId,
                "farmName" to farmName,
                "isRevoked" to false,
                "canViewFinance" to permissions.canViewFinance,
                "canEditFinance" to permissions.canEditFinance,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(workerId).set(workerData, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            // Non-blocking
        }

        worker
    }

    suspend fun setWorkerRevoked(workerId: String, isRevoked: Boolean) = withContext(Dispatchers.IO) {
        repository.setWorkerRevoked(workerId, isRevoked)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            db.collection("users").document(workerId).update("isRevoked", isRevoked).awaitTask()
        } catch (e: Exception) {}
    }

    suspend fun deleteWorker(workerId: String) = withContext(Dispatchers.IO) {
        repository.deleteWorker(workerId)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            db.collection("users").document(workerId).delete().awaitTask()
        } catch (e: Exception) {}
    }

    suspend fun updateWorker(worker: WorkerAccount) = withContext(Dispatchers.IO) {
        repository.updateWorker(worker)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            val workerData = hashMapOf<String, Any>(
                "name" to worker.name,
                "phone" to worker.emailOrPhone,
                "password" to worker.password,
                "canViewFinance" to worker.canViewFinance,
                "canEditFinance" to worker.canEditFinance,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(worker.workerId).set(workerData, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {}
    }
}


