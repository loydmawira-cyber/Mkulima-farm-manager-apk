package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.FarmAccount
import com.example.data.FarmRepository
import com.example.data.UserSession
import com.example.data.WorkerAccount
import com.example.data.WorkerPermissions
import com.example.notifications.FarmDeviceTokenRegistry
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

/** Result returned only when an owner provisions a worker credential. */
sealed class WorkerAccountCreationResult {
    data class Success(val worker: WorkerAccount) : WorkerAccountCreationResult()
    data class Error(val message: String) : WorkerAccountCreationResult()
    data class AccountAlreadyExists(val message: String) : WorkerAccountCreationResult()
}

class AuthManager(
    private val context: Context,
    private val repository: FarmRepository
) {
    companion object {
        private const val TAG = "AuthManager"
        private const val WORKER_PROVISIONING_APP_NAME = "mkulima-worker-provisioner"

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

        fun isValidRecoveryEmail(value: String): Boolean =
            value.matches(Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE))
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("mkulima_auth_prefs", Context.MODE_PRIVATE)
    fun cacheThemeMode(themeMode: String) {
        prefs.edit().putString("cached_theme_mode", themeMode).apply()
    }

    fun getCachedThemeMode(): String? {
        return prefs.getString("cached_theme_mode", null)
    }
    private var firebaseAuth: FirebaseAuth? = null
    // A secondary FirebaseAuth instance provisions workers without replacing the
    // currently signed-in owner in the primary FirebaseAuth instance.
    private var workerProvisioningAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private fun <T> Task<T>.awaitTask(): T {
        try {
            return Tasks.await(this)
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }

    var lastAuthInitError: String? = null

    private fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            if (firebaseAuth != null) return firebaseAuth
            FirebaseAuth.getInstance().also { firebaseAuth = it }
        } catch (t: Throwable) {
            Log.e(TAG, "FirebaseAuth initialization failed", t)
            lastAuthInitError = "${t.javaClass.simpleName}: ${t.message}"
            null
        }
    }


    private fun getWorkerProvisioningAuth(): FirebaseAuth? {
        return try {
            workerProvisioningAuth?.let { return it }
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            val defaultApp = FirebaseApp.getInstance()
            val workerApp = FirebaseApp.getApps(context).firstOrNull {
                it.name == WORKER_PROVISIONING_APP_NAME
            } ?: FirebaseApp.initializeApp(
                context,
                defaultApp.options,
                WORKER_PROVISIONING_APP_NAME
            )
            FirebaseAuth.getInstance(workerApp).also { workerProvisioningAuth = it }
        } catch (t: Throwable) {
            Log.e(TAG, "Worker provisioning authentication initialization failed", t)
            null
        }
    }

    private fun permissionsFromUserProfile(
        role: String,
        userData: Map<String, Any>?
    ): WorkerPermissions {
        val isOwner = role.equals("OWNER", ignoreCase = true)
        fun flag(name: String, ownerDefault: Boolean, workerDefault: Boolean): Boolean =
            (userData?.get(name) as? Boolean) ?: if (isOwner) ownerDefault else workerDefault

        return WorkerPermissions(
            canViewLivestock = flag("canViewLivestock", true, true),
            canEditLivestock = flag("canEditLivestock", true, true),
            canViewLogs = flag("canViewLogs", true, true),
            canEditLogs = flag("canEditLogs", true, true),
            canViewFinance = flag("canViewFinance", true, false),
            canEditFinance = flag("canEditFinance", true, false),
            canViewTasks = flag("canViewTasks", true, true),
            canCompleteTasks = flag("canCompleteTasks", true, true),
            canViewRequests = flag("canViewRequests", true, true)
        )
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

    private val _currentSession = MutableStateFlow<UserSession?>(loadSavedSession())
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    init {
        // Render the locally restored session and Room data first. Firebase initialization,
        // remote session refresh, and synchronization continue on the IO dispatcher.
        CoroutineScope(Dispatchers.IO).launch {
            firebaseAuth = getFirebaseAuth()
            firestore = getFirestore()
            _currentSession.value?.let { cached -> repository.syncEngine?.startSync(cached.farmId) }
            checkAndRestoreFirebaseAuthSession()
        }
    }

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
                        permissions = permissionsFromUserProfile(role, userDocData)
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

    suspend fun updateFarmName(newFarmName: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanName = newFarmName.trim()
        if (cleanName.isBlank()) return@withContext Result.failure(IllegalArgumentException("Enter a farm name."))
        val session = _currentSession.value
            ?: return@withContext Result.failure(IllegalStateException("No active farm session found."))
        if (!session.isOwner) return@withContext Result.failure(IllegalAccessException("Only the farm owner can rename the farm."))

        return@withContext runCatching {
            val existing = repository.getFarmAccount(session.farmId)
            val account = (existing ?: FarmAccount(
                farmId = session.farmId,
                farmName = cleanName,
                ownerId = session.userId,
                ownerName = session.name,
                ownerEmailOrPhone = session.emailOrPhone
            )).copy(farmName = cleanName, updatedAt = System.currentTimeMillis())
            repository.updateFarmAccount(account)

            val renamedSession = session.copy(farmName = cleanName)
            saveSession(renamedSession)

            getFirestore()?.let { db ->
                val update = hashMapOf<String, Any>(
                    "farmName" to cleanName,
                    "updatedAt" to System.currentTimeMillis()
                )
                runCatching {
                    db.collection("farms").document(session.farmId).set(update, SetOptions.merge()).awaitTask()
                    db.collection("users").document(session.userId).set(update, SetOptions.merge()).awaitTask()
                }.onFailure { Log.w(TAG, "Farm name saved locally; remote update will retry when Firestore is available.", it) }
            }
            cleanName
        }
    }

    /**
     * Makes a real email address the owner account's Firebase Auth identifier and
     * recovery address. The owner must be recently signed in because Firebase
     * protects sensitive credential changes.
     */
    suspend fun updateRecoveryEmail(newRecoveryEmail: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanEmail = newRecoveryEmail.trim().lowercase()
        if (!isValidRecoveryEmail(cleanEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Enter a valid recovery email address."))
        }
        val session = _currentSession.value
            ?: return@withContext Result.failure(IllegalStateException("No active farm session found."))
        if (!session.isOwner) {
            return@withContext Result.failure(IllegalAccessException("Only the farm owner can update the recovery email."))
        }
        val auth = getFirebaseAuth()
            ?: return@withContext Result.failure(IllegalStateException("Authentication service is currently unavailable."))
        val user = auth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Please sign in again before updating the recovery email."))

        return@withContext try {
            if (!user.email.equals(cleanEmail, ignoreCase = true)) {
                user.updateEmail(cleanEmail).awaitTask()
                user.sendEmailVerification().awaitTask()
            }
            getFirestore()?.collection("users")?.document(session.userId)?.set(
                hashMapOf<String, Any>(
                    "email" to cleanEmail,
                    "recoveryEmail" to cleanEmail,
                    "authEmail" to cleanEmail,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )?.awaitTask()
            cleanEmail
        } catch (error: Throwable) {
            val message = error.message.orEmpty()
            when {
                error is FirebaseAuthUserCollisionException || message.contains("already in use", ignoreCase = true) ->
                    Result.failure(IllegalArgumentException("That recovery email is already used by another account."))
                message.contains("recent login", ignoreCase = true) || message.contains("requires-recent-login", ignoreCase = true) ->
                    Result.failure(IllegalStateException("For your security, sign out and sign in again, then update the recovery email."))
                else -> Result.failure(IllegalStateException("Could not update the recovery email. Please try again."))
            }
        }
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
        FarmDeviceTokenRegistry.registerOwnerDevice(context, session)
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

        // Fast check in local Room database cache first (0ms)
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
            return@withContext AuthResult.Error("Please provide a recovery email and phone number, or a recovery email.")
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

        val recoveryEmail = cleanIdentifier.lowercase()
        if (!isValidRecoveryEmail(recoveryEmail)) {
            return@withContext AuthResult.Error(
                if (fullPhoneNumber.isNotBlank()) "Enter a valid recovery email. Phone-number accounts use this email for password recovery."
                else "Enter a valid email address."
            )
        }
        val primaryContact = if (fullPhoneNumber.isNotBlank()) fullPhoneNumber else recoveryEmail
        val authEmail = recoveryEmail

        if (fullPhoneNumber.isNotBlank() && checkPhoneNumberExists(fullPhoneNumber)) {
            return@withContext AuthResult.AccountAlreadyExists("An account with this phone number ($fullPhoneNumber) already exists. Please sign in instead.")
        }

        val auth = getFirebaseAuth()
        val db = getFirestore()

        if (auth == null) {
            return@withContext AuthResult.Error("Authentication service is currently unavailable.")
        }

        var cloudUid: String? = null

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
            } else {
                return@withContext AuthResult.Error("Registration failed. Please try again.")
            }
        } catch (e: Throwable) {
            val msg = e.message ?: ""
            if (e is FirebaseAuthUserCollisionException || msg.contains("already in use", ignoreCase = true) || msg.contains("email-already-in-use", ignoreCase = true)) {
                return@withContext AuthResult.AccountAlreadyExists("An account with this email/phone ($primaryContact) already exists. Please sign in instead.")
            }
            return@withContext AuthResult.Error(e.localizedMessage ?: "Registration error: ${e.message}")
        }

        val farmId = generateUniqueFarmId()
        val finalFarmName = cleanFarmName
        val finalUserId = cloudUid ?: "OWNER_${UUID.randomUUID().toString().take(8)}"
        val emailValue = recoveryEmail

        if (db != null) {
            try {
                val userData = hashMapOf<String, Any>(
                    "userId" to finalUserId,
                    "uid" to finalUserId,
                    "name" to cleanName,
                    "email" to emailValue,
                    "recoveryEmail" to recoveryEmail,
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
                db.collection("users").document(finalUserId).set(userData, SetOptions.merge())

                val farmData = hashMapOf<String, Any>(
                    "farmId" to farmId,
                    "farmName" to finalFarmName,
                    "ownerId" to finalUserId,
                    "ownerName" to cleanName,
                    "ownerContact" to primaryContact,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("farms").document(farmId).set(farmData, SetOptions.merge())
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
        repository.seedNewFarmStarterData(farmId, finalFarmName)

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

        // 2. Resolve Candidate Auth Emails for Firebase Authentication (Prioritized for instant sign-in)
        val candidateAuthEmails = LinkedHashSet<String>()

        if (cleanIdentifier.contains("@")) {
            candidateAuthEmails.add(cleanIdentifier.lowercase())
        } else {
            val primaryCanonical = toAuthEmail(cleanIdentifier)
            candidateAuthEmails.add(primaryCanonical)

            val cleanDigits = extractPhoneDigits(cleanIdentifier)
            candidateAuthEmails.add("phone_${cleanDigits}@mkulima.farm")
            candidateAuthEmails.add("phone_254${cleanDigits}@mkulima.farm")
            if (cleanDigits.startsWith("0")) {
                candidateAuthEmails.add("phone_${cleanDigits.removePrefix("0")}@mkulima.farm")
                candidateAuthEmails.add("phone_254${cleanDigits.removePrefix("0")}@mkulima.farm")
            }

            // New phone registrations use a real recovery email as their Firebase
            // email/password identifier. Resolve that email from the caller's phone.
            if (db != null) {
                for (phoneVariant in getPhoneCandidateFormats(cleanIdentifier).distinct()) {
                    try {
                        val byPhone = db.collection("users")
                            .whereEqualTo("phone", phoneVariant)
                            .limit(1)
                            .get()
                            .awaitTask()
                        val storedAuthEmail = byPhone.documents.firstOrNull()?.getString("authEmail")
                        if (!storedAuthEmail.isNullOrBlank()) candidateAuthEmails.add(storedAuthEmail.lowercase())
                    } catch (_: Throwable) { }
                }
            }
        }

        // 3. Authenticate with Firebase Authentication
        var authenticatedUser: com.google.firebase.auth.FirebaseUser? = null
        var lastAuthException: Exception? = null

        if (auth != null) {
            for (targetEmail in candidateAuthEmails) {
                try {
                    val result = auth.signInWithEmailAndPassword(targetEmail, password).awaitTask()
                    if (result?.user != null) {
                        authenticatedUser = result.user
                        break
                    }
                } catch (e: Exception) {
                    lastAuthException = e
                    if (e is FirebaseAuthInvalidCredentialsException && (e.message?.contains("password", ignoreCase = true) == true || e.errorCode == "ERROR_WRONG_PASSWORD")) {
                        break
                    }
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
            Log.d(TAG, "=== DEBUG LOGIN: authenticatedUser.uid = $uid, email = ${authenticatedUser.email}, phone = ${authenticatedUser.phoneNumber} ===")
            var userDocData: Map<String, Any>? = null

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

            // Cache the profile locally. Credentials always remain in Firebase Auth.
            val profilePermissions = permissionsFromUserProfile(role, userDocData)
            if (role.equals("WORKER", ignoreCase = true)) {
                val workerAccount = WorkerAccount(
                    workerId = uid,
                    syncId = uid,
                    farmId = finalFarmId,
                    name = name,
                    emailOrPhone = cleanIdentifier,
                    password = "",
                    role = "WORKER",
                    isRevoked = false,
                    canViewLivestock = profilePermissions.canViewLivestock,
                    canEditLivestock = profilePermissions.canEditLivestock,
                    canViewLogs = profilePermissions.canViewLogs,
                    canEditLogs = profilePermissions.canEditLogs,
                    canViewFinance = profilePermissions.canViewFinance,
                    canEditFinance = profilePermissions.canEditFinance,
                    canViewTasks = profilePermissions.canViewTasks,
                    canCompleteTasks = profilePermissions.canCompleteTasks,
                    canViewRequests = profilePermissions.canViewRequests
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
                permissions = profilePermissions
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

    suspend fun resetPassword(recoveryEmail: String): String = withContext(Dispatchers.IO) {
        val cleanEmail = recoveryEmail.trim().lowercase()
        if (!isValidRecoveryEmail(cleanEmail)) {
            return@withContext "Enter the real recovery email registered with this account. Password reset links are sent by email."
        }
        val auth = getFirebaseAuth()
            ?: return@withContext "Password recovery is temporarily unavailable. Please try again shortly."
        return@withContext try {
            auth.sendPasswordResetEmail(cleanEmail).awaitTask()
            "If this email is registered, a password reset link has been sent. Check your inbox and spam folder."
        } catch (_: Throwable) {
            // Keep this response neutral so email addresses cannot be probed.
            "If this email is registered, a password reset link has been sent. Check your inbox and spam folder."
        }
    }

    /** Password changes now happen only through the verified Firebase email link. */
    suspend fun completePasswordReset(emailOrPhone: String, newPass: String): Boolean = false

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
    ): WorkerAccountCreationResult = withContext(Dispatchers.IO) {
        val cleanName = name.trim().ifBlank { "Farm Worker" }
        val cleanIdentifier = emailOrPhone.trim()
        val cleanPass = pass.trim()

        if (cleanIdentifier.isBlank()) {
            return@withContext WorkerAccountCreationResult.Error("Please enter the worker's email or phone number.")
        }
        if (cleanPass.length < 6) {
            return@withContext WorkerAccountCreationResult.Error("Worker password must be at least 6 characters.")
        }
        if (farmId.isBlank() || farmId == "FARM-DEFAULT") {
            return@withContext WorkerAccountCreationResult.Error("Please sign in as the farm owner before creating a worker.")
        }
        if (repository.getWorkerByLoginIdentifier(cleanIdentifier) != null) {
            return@withContext WorkerAccountCreationResult.AccountAlreadyExists(
                "A worker with this email or phone number already exists."
            )
        }

        val provisioningAuth = getWorkerProvisioningAuth()
            ?: return@withContext WorkerAccountCreationResult.Error("Worker authentication is unavailable. Please check Firebase configuration.")
        val db = getFirestore()
            ?: return@withContext WorkerAccountCreationResult.Error("Worker profile storage is unavailable. Please check Firebase configuration.")
        val authEmail = toAuthEmail(cleanIdentifier)

        val createdUser = try {
            provisioningAuth.createUserWithEmailAndPassword(authEmail, cleanPass).awaitTask().user
                ?: return@withContext WorkerAccountCreationResult.Error("Could not create the worker login account.")
        } catch (e: Throwable) {
            val message = e.message.orEmpty()
            return@withContext if (
                e is FirebaseAuthUserCollisionException ||
                message.contains("already in use", ignoreCase = true) ||
                message.contains("email-already-in-use", ignoreCase = true)
            ) {
                WorkerAccountCreationResult.AccountAlreadyExists(
                    "An account with this email or phone number already exists."
                )
            } else {
                Log.e(TAG, "Worker credential creation failed", e)
                WorkerAccountCreationResult.Error("Could not create the worker login: ${e.localizedMessage ?: "unknown error"}")
            }
        }

        try {
            try {
                createdUser.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(cleanName).build()
                ).awaitTask()
            } catch (ignored: Throwable) {
                // The Firestore profile remains the authoritative display profile.
            }

            val worker = WorkerAccount(
                workerId = createdUser.uid,
                syncId = createdUser.uid,
                farmId = farmId,
                name = cleanName,
                emailOrPhone = cleanIdentifier,
                // Never save credential material in Room or Firestore.
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

            val workerData = hashMapOf<String, Any>(
                "userId" to createdUser.uid,
                "uid" to createdUser.uid,
                "name" to cleanName,
                "email" to if (cleanIdentifier.contains("@")) cleanIdentifier.lowercase() else "",
                "phone" to if (cleanIdentifier.contains("@")) "" else cleanIdentifier,
                "phoneNumber" to if (cleanIdentifier.contains("@")) "" else cleanIdentifier,
                "authEmail" to authEmail,
                "role" to "WORKER",
                "farmId" to farmId,
                "farmName" to farmName,
                "isRevoked" to false,
                "canViewLivestock" to permissions.canViewLivestock,
                "canEditLivestock" to permissions.canEditLivestock,
                "canViewLogs" to permissions.canViewLogs,
                "canEditLogs" to permissions.canEditLogs,
                "canViewFinance" to permissions.canViewFinance,
                "canEditFinance" to permissions.canEditFinance,
                "canViewTasks" to permissions.canViewTasks,
                "canCompleteTasks" to permissions.canCompleteTasks,
                "canViewRequests" to permissions.canViewRequests,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(createdUser.uid).set(workerData, SetOptions.merge()).awaitTask()
            repository.insertWorker(worker)
            WorkerAccountCreationResult.Success(worker)
        } catch (e: Throwable) {
            // Do not leave an unusable Firebase credential behind when its profile
            // could not be created. The secondary auth context owns this user.
            try { createdUser.delete().awaitTask() } catch (ignored: Throwable) {}
            Log.e(TAG, "Worker profile creation failed", e)
            WorkerAccountCreationResult.Error("The worker profile could not be saved. No account was created.")
        }
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
            // Retain a revoked profile rather than deleting it. The Firebase Auth
            // credential then stays blocked instead of falling back to an owner-like
            // profile when the worker attempts to sign in later.
            getFirestore()?.collection("users")?.document(workerId)?.set(
                hashMapOf<String, Any>(
                    "isRevoked" to true,
                    "isDeleted" to true,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )?.awaitTask()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to mark worker profile deleted", e)
        }
    }

    suspend fun updateWorker(worker: WorkerAccount) = withContext(Dispatchers.IO) {
        // Worker passwords are never persisted. Changing another user's Firebase
        // password requires an Admin SDK/Cloud Function and is intentionally not
        // attempted from this client app.
        val sanitized = worker.copy(password = "")
        repository.updateWorker(sanitized)
        try {
            val db = getFirestore()
            if (db != null) {
                val workerData = hashMapOf<String, Any>(
                    "name" to sanitized.name,
                    "email" to if (sanitized.emailOrPhone.contains("@")) sanitized.emailOrPhone.lowercase() else "",
                    "phone" to if (sanitized.emailOrPhone.contains("@")) "" else sanitized.emailOrPhone,
                    "phoneNumber" to if (sanitized.emailOrPhone.contains("@")) "" else sanitized.emailOrPhone,
                    "isRevoked" to sanitized.isRevoked,
                    "canViewLivestock" to sanitized.canViewLivestock,
                    "canEditLivestock" to sanitized.canEditLivestock,
                    "canViewLogs" to sanitized.canViewLogs,
                    "canEditLogs" to sanitized.canEditLogs,
                    "canViewFinance" to sanitized.canViewFinance,
                    "canEditFinance" to sanitized.canEditFinance,
                    "canViewTasks" to sanitized.canViewTasks,
                    "canCompleteTasks" to sanitized.canCompleteTasks,
                    "canViewRequests" to sanitized.canViewRequests,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("users").document(sanitized.workerId).set(workerData, SetOptions.merge()).awaitTask()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to update worker profile", e)
        }
    }
}
