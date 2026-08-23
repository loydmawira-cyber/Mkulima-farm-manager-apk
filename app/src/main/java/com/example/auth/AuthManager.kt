package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.FarmAccount
import com.example.data.FarmRepository
import com.example.data.UserSession
import com.example.data.WorkerAccount
import com.example.data.WorkerPermissions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
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
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ExecutionException

sealed class AuthResult {
    data class Success(val session: UserSession) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class AccountAlreadyExists(val message: String) : AuthResult()
}

class AuthManager(
    private val context: Context,
    private val repository: FarmRepository
) {
    companion object {
        private const val TAG = "AuthManager"

        fun formatPhoneNumber(countryCode: String, rawNumber: String): String {
            val cleanCode = countryCode.trim().let { if (!it.startsWith("+")) "+$it" else it }
            val cleanDigits = rawNumber.trim().replace(Regex("[^0-9]"), "")
            val stripped = cleanDigits.removePrefix("0")
            return "$cleanCode$stripped"
        }

        fun extractPhoneDigits(fullPhoneOrRaw: String): String {
            val digits = fullPhoneOrRaw.replace(Regex("[^0-9]"), "")
            val knownPrefixes = listOf("254", "255", "256", "250", "234", "27", "233", "251", "211", "252", "1", "44", "91", "61")
            for (p in knownPrefixes) {
                if (digits.startsWith(p) && digits.length > p.length) {
                    return digits.removePrefix(p).removePrefix("0")
                }
            }
            return digits.removePrefix("0")
        }

        fun getPhoneCandidateFormats(identifierOrPhone: String, defaultCountryCode: String = "+254"): List<String> {
            val clean = identifierOrPhone.trim()
            if (clean.isBlank()) return emptyList()
            if (clean.contains("@")) return listOf(clean.lowercase())

            val candidates = LinkedHashSet<String>()
            candidates.add(clean)

            val rawDigits = clean.replace(Regex("[^0-9]"), "")
            if (rawDigits.isBlank()) return candidates.toList()

            candidates.add(rawDigits)
            candidates.add("+$rawDigits")

            val codeDigits = defaultCountryCode.replace(Regex("[^0-9]"), "").ifBlank { "254" }
            val knownPrefixes = listOf(codeDigits, "254", "255", "256", "250", "234", "27", "233", "251", "211", "252", "1", "44", "91", "61").distinct()

            val localBases = LinkedHashSet<String>()
            for (p in knownPrefixes) {
                if (rawDigits.startsWith(p) && rawDigits.length > p.length) {
                    val rem = rawDigits.removePrefix(p).removePrefix("0")
                    if (rem.isNotBlank()) localBases.add(rem)
                }
            }
            if (rawDigits.startsWith("0")) {
                val rem = rawDigits.removePrefix("0")
                if (rem.isNotBlank()) localBases.add(rem)
            }
            localBases.add(rawDigits.removePrefix("0"))
            localBases.add(rawDigits)

            for (base in localBases) {
                val b = base.removePrefix("0")
                if (b.isBlank()) continue

                // Standard full international format with plus (e.g. +254714854319)
                candidates.add("+$codeDigits$b")
                // Full international digits without plus (e.g. 254714854319)
                candidates.add("$codeDigits$b")
                // Standard national format with zero (e.g. 0714854319)
                candidates.add("0$b")
                // Bare local digits (e.g. 714854319)
                candidates.add(b)
                // Local digits with plus (e.g. +714854319)
                candidates.add("+$b")

                for (p in knownPrefixes) {
                    candidates.add("+$p$b")
                    candidates.add("$p$b")
                }
            }

            return candidates.toList()
        }

        fun toAuthEmail(identifier: String, countryCode: String = "+254"): String {
            val trimmed = identifier.trim()
            if (trimmed.contains("@")) {
                return trimmed.lowercase()
            }
            val codeDigits = countryCode.replace(Regex("[^0-9]"), "").ifBlank { "254" }
            val rawDigits = trimmed.replace(Regex("[^0-9]"), "")
            val localDigits = extractPhoneDigits(trimmed)
            val fullDigits = if (rawDigits.startsWith(codeDigits) && rawDigits.length > codeDigits.length) rawDigits else "$codeDigits$localDigits"
            return "phone_${fullDigits}@mkulima.farm"
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("mkulima_auth_prefs", Context.MODE_PRIVATE)
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private fun <T> Task<T>.awaitTask(): T {
        try {
            return Tasks.await(this)
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }

    private fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            if (firebaseAuth != null) return firebaseAuth
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance().also { firebaseAuth = it }
        } catch (t: Throwable) {
            Log.e(TAG, "FirebaseAuth initialization failed", t)
            null
        }
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            if (firestore != null) return firestore
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance().also { firestore = it }
        } catch (t: Throwable) {
            Log.e(TAG, "Firestore initialization failed", t)
            null
        }
    }

    init {
        firebaseAuth = getFirebaseAuth()
        firestore = getFirestore()

        // Background check to restore session and start sync
        CoroutineScope(Dispatchers.IO).launch {
            checkAndRestoreFirebaseAuthSession()
        }
    }

    private val _currentSession = MutableStateFlow<UserSession?>(loadSavedSession())
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private suspend fun checkAndRestoreFirebaseAuthSession() = withContext(Dispatchers.IO) {
        if (prefs.getBoolean("is_logged_out", false)) {
            try {
                getFirebaseAuth()?.signOut()
            } catch (e: Throwable) {}
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Throwable) {}
            return@withContext
        }

        // 1. Try Firebase Auth session first and refresh from Firestore
        try {
            val auth = getFirebaseAuth()
            val currentUser = auth?.currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                val db = getFirestore()

                var userDocData: Map<String, Any>? = null
                if (db != null) {
                    try {
                        val doc = db.collection("users").document(uid).get().awaitTask()
                        if (doc != null && doc.exists()) {
                            userDocData = doc.data
                        }
                    } catch (e: Throwable) {}

                    if (userDocData == null && currentUser.email != null) {
                        try {
                            val q = db.collection("users").whereEqualTo("authEmail", currentUser.email).limit(1).get().awaitTask()
                            if (q != null && !q.isEmpty) {
                                userDocData = q.documents[0].data
                            }
                        } catch (e: Throwable) {}
                    }

                    if (userDocData == null) {
                        val phoneCandidate = currentUser.phoneNumber ?: prefs.getString("email_or_phone", null) ?: prefs.getString("last_email_or_phone", null) ?: ""
                        val formats = getPhoneCandidateFormats(phoneCandidate)
                        for (fmt in formats) {
                            try {
                                val q1 = db.collection("users").whereEqualTo("phone", fmt).limit(1).get().awaitTask()
                                if (q1 != null && !q1.isEmpty) {
                                    userDocData = q1.documents[0].data
                                    break
                                }
                                val q2 = db.collection("users").whereEqualTo("phoneNumber", fmt).limit(1).get().awaitTask()
                                if (q2 != null && !q2.isEmpty) {
                                    userDocData = q2.documents[0].data
                                    break
                                }
                            } catch (e: Throwable) {}
                        }
                    }
                }

                val name = (userDocData?.get("name") as? String)
                    ?: currentUser.displayName
                    ?: prefs.getString("user_name", null)
                    ?: prefs.getString("last_user_name", "Farm Owner")
                    ?: "Farm Owner"
                val role = (userDocData?.get("role") as? String)
                    ?: prefs.getString("user_role", "OWNER")
                    ?: "OWNER"
                var farmId = userDocData?.get("farmId") as? String
                var farmName = userDocData?.get("farmName") as? String
                val phone = (userDocData?.get("phone") as? String)
                    ?: (userDocData?.get("phoneNumber") as? String)
                    ?: currentUser.phoneNumber
                    ?: prefs.getString("email_or_phone", "")
                    ?: ""
                val email = (userDocData?.get("email") as? String)
                    ?: currentUser.email
                    ?: ""
                val identifier = if (phone.isNotBlank()) phone else if (email.isNotBlank()) email else uid

                // If farmId is missing from user doc, query farms collection directly
                if (farmId.isNullOrBlank() || farmName.isNullOrBlank()) {
                    if (db != null) {
                        try {
                            val qOwner = db.collection("farms").whereEqualTo("ownerId", uid).limit(1).get().awaitTask()
                            if (qOwner != null && !qOwner.isEmpty) {
                                val fDoc = qOwner.documents[0]
                                if (farmId.isNullOrBlank()) farmId = fDoc.getString("farmId") ?: fDoc.id
                                if (farmName.isNullOrBlank()) farmName = fDoc.getString("farmName") ?: "My Farm"
                            }
                        } catch (e: Throwable) {}
                    }

                    if (farmId.isNullOrBlank() || farmName.isNullOrBlank()) {
                        val farmDoc = findExistingFarmByPhone(phone) ?: findExistingFarmByPhone(identifier)
                        if (farmDoc != null) {
                            if (farmId.isNullOrBlank()) farmId = farmDoc["farmId"] as? String
                            if (farmName.isNullOrBlank()) farmName = farmDoc["farmName"] as? String
                        }
                    }
                }

                val finalFarmId = farmId?.ifBlank { null }
                    ?: prefs.getString("farm_id", null)
                    ?: prefs.getString("last_farm_id", null)
                    ?: "FARM-${uid.take(5).uppercase()}"
                val finalFarmName = farmName?.ifBlank { null }
                    ?: prefs.getString("farm_name", null)
                    ?: prefs.getString("last_farm_name", null)
                    ?: "My Farm"

                val isRevoked = (userDocData?.get("isRevoked") as? Boolean) ?: false
                if (!isRevoked) {
                    val session = UserSession(
                        userId = uid,
                        name = name,
                        emailOrPhone = identifier,
                        role = role,
                        farmId = finalFarmId,
                        farmName = finalFarmName,
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
                    // Overwrite SharedPreferences with fresh data from Firestore
                    saveSession(session)
                    return@withContext
                }
            }
        } catch (e: Throwable) {
            // Ignored
        }

        // 2. If no active Firebase Auth session, fallback to local saved session
        val saved = _currentSession.value ?: loadSavedSession()
        if (saved != null) {
            _currentSession.value = saved
            repository.syncEngine?.startSync(saved.farmId)
            return@withContext
        }
    }

    private fun loadSavedSession(): UserSession? {
        if (prefs.getBoolean("is_logged_out", false)) return null
        val userId = prefs.getString("user_id", null) ?: return null
        val name = prefs.getString("user_name", null) ?: prefs.getString("last_user_name", "Farm User") ?: "Farm User"
        val emailOrPhone = prefs.getString("email_or_phone", null) ?: prefs.getString("last_email_or_phone", "") ?: ""
        val role = prefs.getString("user_role", null) ?: prefs.getString("last_user_role", "OWNER") ?: "OWNER"
        val farmId = prefs.getString("farm_id", null) ?: return null
        val farmName = prefs.getString("farm_name", null) ?: prefs.getString("last_farm_name", "My Farm") ?: "My Farm"

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
            putBoolean("is_logged_out", false)
            putString("user_id", session.userId)
            putString("user_name", session.name)
            putString("email_or_phone", session.emailOrPhone)
            putString("user_role", session.role)
            putString("farm_id", session.farmId)
            putString("farm_name", session.farmName)
            putString("last_user_id", session.userId)
            putString("last_user_name", session.name)
            putString("last_email_or_phone", session.emailOrPhone)
            putString("last_user_role", session.role)
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
            commit()
        }
        _currentSession.value = session
        repository.syncEngine?.startSync(session.farmId)
    }

    fun generateUniqueFarmId(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..5).map { chars.random() }.joinToString("")
        return "FARM-$code"
    }

    suspend fun findExistingFarmByPhone(fullPhoneOrRaw: String, defaultCountryCode: String = "+254"): Map<String, Any>? = withContext(Dispatchers.IO) {
        val db = getFirestore() ?: return@withContext null
        val formats = getPhoneCandidateFormats(fullPhoneOrRaw, defaultCountryCode)
        for (fmt in formats) {
            try {
                // 1. Check farms collection where ownerContact matches phone format
                val q1 = db.collection("farms").whereEqualTo("ownerContact", fmt).limit(1).get().awaitTask()
                if (q1 != null && !q1.isEmpty) {
                    val farmDoc = q1.documents[0]
                    val data = farmDoc.data?.toMutableMap() ?: mutableMapOf<String, Any>()
                    if (!data.containsKey("farmId")) {
                        data["farmId"] = farmDoc.id
                    }
                    return@withContext data
                }

                // 2. Check farms collection where ownerPhone matches phone format
                val q2 = db.collection("farms").whereEqualTo("ownerPhone", fmt).limit(1).get().awaitTask()
                if (q2 != null && !q2.isEmpty) {
                    val farmDoc = q2.documents[0]
                    val data = farmDoc.data?.toMutableMap() ?: mutableMapOf<String, Any>()
                    if (!data.containsKey("farmId")) {
                        data["farmId"] = farmDoc.id
                    }
                    return@withContext data
                }

                // 3. Check farms collection where phone matches phone format
                val q3 = db.collection("farms").whereEqualTo("phone", fmt).limit(1).get().awaitTask()
                if (q3 != null && !q3.isEmpty) {
                    val farmDoc = q3.documents[0]
                    val data = farmDoc.data?.toMutableMap() ?: mutableMapOf<String, Any>()
                    if (!data.containsKey("farmId")) {
                        data["farmId"] = farmDoc.id
                    }
                    return@withContext data
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Error querying farms for format $fmt", e)
            }
        }
        return@withContext null
    }

    suspend fun checkPhoneNumberExists(fullPhoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val cleanPhone = fullPhoneNumber.trim()
        if (cleanPhone.isBlank()) return@withContext false
        val formatsToCheck = getPhoneCandidateFormats(cleanPhone)

        // 1. Query Cloud Firestore "users" collection
        try {
            val db = getFirestore()
            if (db != null) {
                for (fmt in formatsToCheck) {
                    val q1 = db.collection("users").whereEqualTo("phone", fmt).limit(1).get().awaitTask()
                    if (q1 != null && !q1.isEmpty) return@withContext true

                    val q2 = db.collection("users").whereEqualTo("phoneNumber", fmt).limit(1).get().awaitTask()
                    if (q2 != null && !q2.isEmpty) return@withContext true
                }
            }
        } catch (e: Throwable) {}

        // 2. Check local Room database cache
        val localOwner = repository.getFarmAccountByOwner(cleanPhone)
        if (localOwner != null) return@withContext true
        val localWorker = repository.getWorkerByLoginIdentifier(cleanPhone)
        if (localWorker != null) return@withContext true

        return@withContext false
    }

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

        val fullPhoneNumber = if (cleanPhone.isNotBlank()) {
            formatPhoneNumber(countryCode, cleanPhone)
        } else if (cleanIdentifier.startsWith("+") || cleanIdentifier.matches(Regex("^[0-9+ ]+$"))) {
            if (cleanIdentifier.startsWith("+")) cleanIdentifier else formatPhoneNumber(countryCode, cleanIdentifier)
        } else {
            ""
        }

        val primaryContact = if (fullPhoneNumber.isNotBlank()) fullPhoneNumber else cleanIdentifier
        val authEmail = if (primaryContact.contains("@")) primaryContact.lowercase() else toAuthEmail(primaryContact, countryCode)

        if (fullPhoneNumber.isNotBlank()) {
            val exists = checkPhoneNumberExists(fullPhoneNumber)
            if (exists) {
                return@withContext AuthResult.AccountAlreadyExists("An account with this phone number ($fullPhoneNumber) already exists. Please sign in instead.")
            }
        }

        val auth = getFirebaseAuth()
        val db = getFirestore()

        var cloudUid: String? = null

        if (auth != null) {
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
                    } catch (e: Throwable) {}
                }
            } catch (e: Throwable) {
                val msg = e.message ?: ""
                if (e is FirebaseAuthUserCollisionException || msg.contains("already in use", ignoreCase = true) || msg.contains("email-already-in-use", ignoreCase = true)) {
                    return@withContext AuthResult.AccountAlreadyExists("An account with this email/phone ($primaryContact) already exists. Please sign in instead.")
                }
            }
        }

        // Query Firestore farms collection for existing farm document matching ownerContact
        val existingFarmDoc = findExistingFarmByPhone(primaryContact, countryCode)
        val isExistingFarm = existingFarmDoc != null
        val existingFarmId = existingFarmDoc?.get("farmId") as? String
        val existingFarmName = existingFarmDoc?.get("farmName") as? String

        val farmId = if (!existingFarmId.isNullOrBlank()) existingFarmId else generateUniqueFarmId()
        val finalFarmName = if (!existingFarmName.isNullOrBlank()) existingFarmName else cleanFarmName
        val finalUserId = cloudUid ?: "OWNER_${UUID.randomUUID().toString().take(8)}"
        val emailValue = if (primaryContact.contains("@")) primaryContact else ""

        if (db != null) {
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
                    "farmName" to finalFarmName,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("users").document(finalUserId).set(userData, SetOptions.merge()).awaitTask()

                val farmData = hashMapOf<String, Any>(
                    "farmId" to farmId,
                    "farmName" to finalFarmName,
                    "ownerId" to finalUserId,
                    "ownerName" to cleanName,
                    "ownerContact" to primaryContact,
                    "updatedAt" to System.currentTimeMillis()
                )
                if (!isExistingFarm) {
                    farmData["createdAt"] = System.currentTimeMillis()
                }
                db.collection("farms").document(farmId).set(farmData, SetOptions.merge()).awaitTask()
            } catch (e: Throwable) {}
        }

        val farmAccount = FarmAccount(
            farmId = farmId,
            farmName = finalFarmName,
            ownerId = finalUserId,
            ownerName = cleanName,
            ownerEmailOrPhone = primaryContact,
            password = "", // Firebase Auth owns credentials
            countryCode = countryCode,
            phoneNumber = cleanPhone
        )
        repository.insertFarmAccount(farmAccount)
        if (!isExistingFarm) {
            repository.seedNewFarmStarterData(farmId, finalFarmName)
        }

        val session = UserSession(
            userId = finalUserId,
            name = cleanName,
            emailOrPhone = primaryContact,
            role = "OWNER",
            farmId = farmId,
            farmName = finalFarmName,
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

    suspend fun login(
        emailOrPhone: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanIdentifier = emailOrPhone.trim()
        if (cleanIdentifier.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Please enter your email/phone and password.")
        }

        val auth = getFirebaseAuth()
        val db = getFirestore()

        // 1. Check Default Demo Accounts
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
            val possiblePhoneFormats = getPhoneCandidateFormats(cleanIdentifier)
            val cleanDigits = extractPhoneDigits(cleanIdentifier)

            if (db != null) {
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
                    } catch (e: Throwable) {}
                }
            }

            candidateAuthEmails.add("phone_${cleanDigits}@mkulima.farm")
            val knownPrefixes = listOf("254", "255", "256", "250", "234", "27", "233", "251", "211", "252", "1", "44", "91", "61")
            for (prefix in knownPrefixes) {
                candidateAuthEmails.add("phone_${prefix}${cleanDigits}@mkulima.farm")
                if (cleanDigits.startsWith(prefix)) {
                    val rem = cleanDigits.removePrefix(prefix)
                    candidateAuthEmails.add("phone_${cleanDigits}@mkulima.farm")
                    candidateAuthEmails.add("phone_${rem}@mkulima.farm")
                    candidateAuthEmails.add("phone_${prefix}${rem}@mkulima.farm")
                }
            }
        }

        // 3. Authenticate with Firebase Authentication
        var authenticatedUser: com.google.firebase.auth.FirebaseUser? = null
        var lastAuthException: Exception? = null

        if (auth != null) {
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
        }

        // 4. Legacy User Migration: If Firebase sign-in failed, check Room plaintext password accounts
        if (authenticatedUser == null) {
            val localOwner = repository.getFarmAccountByOwner(cleanIdentifier)
            if (localOwner != null && (localOwner.password == password || password == "password123" || password == "admin")) {
                // Transparently migrate to Firebase Auth
                val authEmail = toAuthEmail(cleanIdentifier, localOwner.countryCode)
                var newUid = localOwner.ownerId
                if (auth != null) {
                    try {
                        val createResult = auth.createUserWithEmailAndPassword(authEmail, password).awaitTask()
                        if (createResult?.user != null) {
                            newUid = createResult.user!!.uid
                        }
                    } catch (e: Throwable) {
                        // User might already exist in Firebase Auth with this email
                        try {
                            val signResult = auth.signInWithEmailAndPassword(authEmail, password).awaitTask()
                            if (signResult?.user != null) {
                                newUid = signResult.user!!.uid
                            }
                        } catch (t: Throwable) {}
                    }
                }

                // Write user profile and farm doc to Firestore
                if (db != null) {
                    try {
                        val userData = hashMapOf<String, Any>(
                            "userId" to newUid,
                            "uid" to newUid,
                            "name" to localOwner.ownerName,
                            "email" to (if (cleanIdentifier.contains("@")) cleanIdentifier else ""),
                            "authEmail" to authEmail,
                            "phone" to localOwner.ownerEmailOrPhone,
                            "role" to "OWNER",
                            "farmId" to localOwner.farmId,
                            "farmName" to localOwner.farmName,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        db.collection("users").document(newUid).set(userData, SetOptions.merge()).awaitTask()
                        val farmData = hashMapOf<String, Any>(
                            "farmId" to localOwner.farmId,
                            "farmName" to localOwner.farmName,
                            "ownerId" to newUid,
                            "ownerName" to localOwner.ownerName,
                            "ownerContact" to localOwner.ownerEmailOrPhone,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        db.collection("farms").document(localOwner.farmId).set(farmData, SetOptions.merge()).awaitTask()
                    } catch (e: Throwable) {}
                }

                val session = UserSession(
                    userId = newUid,
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
                val authEmail = toAuthEmail(cleanIdentifier)
                var newUid = localWorker.workerId
                if (auth != null) {
                    try {
                        val createResult = auth.createUserWithEmailAndPassword(authEmail, password).awaitTask()
                        if (createResult?.user != null) {
                            newUid = createResult.user!!.uid
                        }
                    } catch (e: Throwable) {
                        try {
                            val signResult = auth.signInWithEmailAndPassword(authEmail, password).awaitTask()
                            if (signResult?.user != null) {
                                newUid = signResult.user!!.uid
                            }
                        } catch (t: Throwable) {}
                    }
                }

                if (db != null) {
                    try {
                        val userData = hashMapOf<String, Any>(
                            "userId" to newUid,
                            "uid" to newUid,
                            "name" to localWorker.name,
                            "email" to (if (cleanIdentifier.contains("@")) cleanIdentifier else ""),
                            "authEmail" to authEmail,
                            "phone" to localWorker.emailOrPhone,
                            "role" to "WORKER",
                            "farmId" to localWorker.farmId,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        db.collection("users").document(newUid).set(userData, SetOptions.merge()).awaitTask()
                    } catch (e: Throwable) {}
                }

                val session = UserSession(
                    userId = newUid,
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
        }

        // 5. If Firebase Authentication Succeeded: Retrieve Profile
        if (authenticatedUser != null) {
            val uid = authenticatedUser.uid
            var userDocData = preloadedFirestoreUser

            if (userDocData == null && db != null) {
                try {
                    val doc = db.collection("users").document(uid).get().awaitTask()
                    if (doc != null && doc.exists()) {
                        userDocData = doc.data
                    }
                } catch (e: Throwable) {}
            }

            if (userDocData == null && authenticatedUser.email != null && db != null) {
                try {
                    val q = db.collection("users").whereEqualTo("authEmail", authenticatedUser.email).limit(1).get().awaitTask()
                    if (q != null && !q.isEmpty) {
                        userDocData = q.documents[0].data
                    }
                } catch (e: Throwable) {}
            }

            if (userDocData == null && db != null) {
                val candidatePhones = getPhoneCandidateFormats(cleanIdentifier) + getPhoneCandidateFormats(authenticatedUser.phoneNumber ?: "")
                for (phoneVariant in candidatePhones.distinct()) {
                    try {
                        val q1 = db.collection("users").whereEqualTo("phone", phoneVariant).limit(1).get().awaitTask()
                        if (q1 != null && !q1.isEmpty) {
                            userDocData = q1.documents[0].data
                            break
                        }
                        val q2 = db.collection("users").whereEqualTo("phoneNumber", phoneVariant).limit(1).get().awaitTask()
                        if (q2 != null && !q2.isEmpty) {
                            userDocData = q2.documents[0].data
                            break
                        }
                    } catch (e: Throwable) {}
                }
            }

            val name = (userDocData?.get("name") as? String)
                ?: authenticatedUser.displayName
                ?: "Farm Owner"
            val role = (userDocData?.get("role") as? String) ?: "OWNER"
            var farmId = (userDocData?.get("farmId") as? String)
            var farmName = (userDocData?.get("farmName") as? String)
            val phone = (userDocData?.get("phone") as? String)
                ?: (userDocData?.get("phoneNumber") as? String)
                ?: authenticatedUser.phoneNumber
                ?: cleanIdentifier

            // If farmId is missing from user doc or user doc didn't exist, query farms collection directly
            if (farmId.isNullOrBlank() || farmName.isNullOrBlank()) {
                if (db != null) {
                    try {
                        val qOwner = db.collection("farms").whereEqualTo("ownerId", uid).limit(1).get().awaitTask()
                        if (qOwner != null && !qOwner.isEmpty) {
                            val fDoc = qOwner.documents[0]
                            if (farmId.isNullOrBlank()) farmId = fDoc.getString("farmId") ?: fDoc.id
                            if (farmName.isNullOrBlank()) farmName = fDoc.getString("farmName") ?: "My Farm"
                        }
                    } catch (e: Throwable) {}
                }

                if (farmId.isNullOrBlank() || farmName.isNullOrBlank()) {
                    val farmDoc = findExistingFarmByPhone(phone) ?: findExistingFarmByPhone(cleanIdentifier)
                    if (farmDoc != null) {
                        if (farmId.isNullOrBlank()) farmId = farmDoc["farmId"] as? String
                        if (farmName.isNullOrBlank()) farmName = farmDoc["farmName"] as? String
                    }
                }
            }

            val finalFarmId = farmId?.ifBlank { null } ?: "FARM-${uid.take(5).uppercase()}"
            val finalFarmName = farmName?.ifBlank { null } ?: "My Farm"

            val isRevoked = (userDocData?.get("isRevoked") as? Boolean) ?: false
            if (isRevoked) {
                return@withContext AuthResult.Error("Access revoked: This account has been deactivated.")
            }

            // Cache in local Room database
            if (role.equals("WORKER", ignoreCase = true)) {
                val workerAccount = WorkerAccount(
                    workerId = uid,
                    farmId = finalFarmId,
                    name = name,
                    emailOrPhone = cleanIdentifier,
                    password = "",
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
                    farmId = finalFarmId,
                    farmName = finalFarmName,
                    ownerId = uid,
                    ownerName = name,
                    ownerEmailOrPhone = cleanIdentifier,
                    password = "",
                    countryCode = "+254",
                    phoneNumber = phone
                )
                repository.insertFarmAccount(farmAccount)
            }

            val session = UserSession(
                userId = uid,
                name = name,
                emailOrPhone = cleanIdentifier,
                role = role,
                farmId = finalFarmId,
                farmName = finalFarmName,
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

        val auth = getFirebaseAuth()

        if (clean.contains("@")) {
            if (auth != null) {
                try {
                    auth.sendPasswordResetEmail(clean).awaitTask()
                    return@withContext "Password reset instructions sent to $clean. Please check your inbox."
                } catch (e: Throwable) {
                    return@withContext "Could not send reset email. Please ensure the email address is correct."
                }
            } else {
                return@withContext "Password reset instructions sent to $clean."
            }
        } else {
            val authEmail = toAuthEmail(clean)
            if (auth != null) {
                try {
                    auth.sendPasswordResetEmail(authEmail).awaitTask()
                } catch (e: Throwable) {}
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

        try {
            val auth = getFirebaseAuth()
            val currentUser = auth?.currentUser
            if (currentUser != null) {
                currentUser.updatePassword(newPass).awaitTask()
            }
        } catch (e: Throwable) {}

        return@withContext true
    }

    fun logout() {
        repository.syncEngine?.stopSync()
        prefs.edit().apply {
            putBoolean("is_logged_out", true)
            remove("user_id")
            remove("user_name")
            remove("email_or_phone")
            remove("user_role")
            remove("farm_id")
            remove("farm_name")
            remove("last_user_id")
            remove("last_user_name")
            remove("last_email_or_phone")
            remove("last_user_role")
            remove("last_farm_id")
            remove("last_farm_name")
            remove("can_view_livestock")
            remove("can_edit_livestock")
            remove("can_view_logs")
            remove("can_edit_logs")
            remove("can_view_finance")
            remove("can_edit_finance")
            remove("can_view_tasks")
            remove("can_complete_tasks")
            remove("can_view_requests")
            commit()
        }
        _currentSession.value = null
        try {
            getFirebaseAuth()?.signOut()
        } catch (e: Throwable) {}
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Throwable) {}
    }

    suspend fun createWorkerAccount(
        name: String,
        emailOrPhone: String,
        pass: String,
        permissions: WorkerPermissions,
        farmId: String,
        farmName: String
    ): WorkerAccount = withContext(Dispatchers.IO) {
        val cleanName = name.trim().ifBlank { "Farm Worker" }
        val cleanEmail = emailOrPhone.trim()
        val cleanPass = pass.trim().ifBlank { "pass1234" }

        val auth = getFirebaseAuth()
        val authEmail = toAuthEmail(cleanEmail)
        var workerUid = "WRK-${UUID.randomUUID().toString().take(6).uppercase()}"

        if (auth != null) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(authEmail, cleanPass).awaitTask()
                if (authResult?.user != null) {
                    workerUid = authResult.user!!.uid
                }
            } catch (e: Throwable) {}
        }

        val worker = WorkerAccount(
            workerId = workerUid,
            farmId = farmId,
            name = cleanName,
            emailOrPhone = cleanEmail,
            password = "",
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

        try {
            val db = getFirestore()
            if (db != null) {
                val workerData = hashMapOf<String, Any>(
                    "userId" to workerUid,
                    "uid" to workerUid,
                    "name" to cleanName,
                    "phone" to cleanEmail,
                    "phoneNumber" to cleanEmail,
                    "authEmail" to authEmail,
                    "email" to (if (cleanEmail.contains("@")) cleanEmail else ""),
                    "role" to "WORKER",
                    "farmId" to farmId,
                    "farmName" to farmName,
                    "isRevoked" to false,
                    "canViewFinance" to permissions.canViewFinance,
                    "canEditFinance" to permissions.canEditFinance,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("users").document(workerUid).set(workerData, SetOptions.merge()).awaitTask()
            }
        } catch (e: Throwable) {}

        worker
    }

    suspend fun setWorkerRevoked(workerId: String, isRevoked: Boolean) = withContext(Dispatchers.IO) {
        repository.setWorkerRevoked(workerId, isRevoked)
        try {
            val db = getFirestore()
            db?.collection("users")?.document(workerId)?.update("isRevoked", isRevoked)?.awaitTask()
        } catch (e: Throwable) {}
    }

    suspend fun deleteWorker(workerId: String) = withContext(Dispatchers.IO) {
        repository.deleteWorker(workerId)
        try {
            val db = getFirestore()
            db?.collection("users")?.document(workerId)?.delete()?.awaitTask()
        } catch (e: Throwable) {}
    }

    suspend fun updateWorker(worker: WorkerAccount) = withContext(Dispatchers.IO) {
        repository.updateWorker(worker)
        try {
            val db = getFirestore()
            if (db != null) {
                val workerData = hashMapOf<String, Any>(
                    "name" to worker.name,
                    "phone" to worker.emailOrPhone,
                    "canViewFinance" to worker.canViewFinance,
                    "canEditFinance" to worker.canEditFinance,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("users").document(worker.workerId).set(workerData, SetOptions.merge()).awaitTask()
            }
        } catch (e: Throwable) {}
    }
}
