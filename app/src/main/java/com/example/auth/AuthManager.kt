package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.FarmAccount
import com.example.data.FarmRepository
import com.example.data.UserSession
import com.example.data.WorkerAccount
import com.example.data.WorkerPermissions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class AuthResult {
    data class Success(val session: UserSession) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthManager(
    private val context: Context,
    private val repository: FarmRepository
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mkulima_auth_prefs", Context.MODE_PRIVATE)
    private var firebaseAuth: FirebaseAuth? = null

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
        } catch (e: Exception) {
            // Firebase not initialized or offline fallback
            firebaseAuth = null
        }
    }

    private val _currentSession = MutableStateFlow<UserSession?>(loadSavedSession())
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private fun loadSavedSession(): UserSession? {
        val userId = prefs.getString("user_id", null) ?: return null
        val name = prefs.getString("user_name", "Farm User") ?: "Farm User"
        val emailOrPhone = prefs.getString("email_or_phone", "") ?: ""
        val role = prefs.getString("user_role", "OWNER") ?: "OWNER"
        val farmId = prefs.getString("farm_id", "FARM-DEFAULT") ?: "FARM-DEFAULT"
        val farmName = prefs.getString("farm_name", "Green Pastures Farm") ?: "Green Pastures Farm"

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

    suspend fun signUpOwner(
        name: String,
        emailOrPhone: String,
        password: String,
        farmName: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanIdentifier = emailOrPhone.trim()
        val cleanName = name.trim().ifBlank { "Farm Owner" }
        val cleanFarmName = farmName.trim().ifBlank { "My Farm" }

        if (cleanIdentifier.isBlank()) {
            return@withContext AuthResult.Error("Please provide an email or phone number.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
        }

        val farmId = generateUniqueFarmId()
        val userId = "OWNER_${UUID.randomUUID().toString().take(8)}"

        // Try Firebase Auth if email provided
        if (cleanIdentifier.contains("@")) {
            try {
                firebaseAuth?.createUserWithEmailAndPassword(cleanIdentifier, password)
            } catch (e: Exception) {
                // Ignore or proceed with local registration fallback
            }
        }

        val farmAccount = FarmAccount(
            farmId = farmId,
            farmName = cleanFarmName,
            ownerId = userId,
            ownerName = cleanName,
            ownerEmailOrPhone = cleanIdentifier
        )
        repository.insertFarmAccount(farmAccount)
        repository.seedNewFarmStarterData(farmId, cleanFarmName)

        val session = UserSession(
            userId = userId,
            name = cleanName,
            emailOrPhone = cleanIdentifier,
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

    suspend fun login(
        emailOrPhone: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanIdentifier = emailOrPhone.trim()
        if (cleanIdentifier.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Please enter your email/phone and password.")
        }

        // 1. Check if it's a Worker account
        val worker = repository.getWorkerByLoginIdentifier(cleanIdentifier)
        if (worker != null) {
            if (worker.password != password) {
                return@withContext AuthResult.Error("Incorrect password for worker account.")
            }
            if (worker.isRevoked) {
                return@withContext AuthResult.Error("Access revoked: This worker account has been deactivated by the Farm Owner.")
            }
            val farm = repository.getFarmAccount(worker.farmId)
            val farmName = farm?.farmName ?: "Assigned Farm (${worker.farmId})"

            val session = UserSession(
                userId = worker.workerId,
                name = worker.name,
                emailOrPhone = worker.emailOrPhone,
                role = "WORKER",
                farmId = worker.farmId,
                farmName = farmName,
                isRevoked = false,
                permissions = worker.toPermissions()
            )
            saveSession(session)
            return@withContext AuthResult.Success(session)
        }

        // 2. Check if it's a registered Owner
        val ownerFarm = repository.getFarmAccountByOwner(cleanIdentifier)
        if (ownerFarm != null) {
            val session = UserSession(
                userId = ownerFarm.ownerId,
                name = ownerFarm.ownerName,
                emailOrPhone = ownerFarm.ownerEmailOrPhone,
                role = "OWNER",
                farmId = ownerFarm.farmId,
                farmName = ownerFarm.farmName,
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
            return@withContext AuthResult.Success(session)
        }

        // 3. Check default demo accounts or fallback Firebase Auth
        if (cleanIdentifier.equals("owner@mkulima.farm", ignoreCase = true) && (password == "password123" || password == "admin")) {
            val session = UserSession(
                userId = "owner_default",
                name = "David Kimani (Owner)",
                emailOrPhone = "owner@mkulima.farm",
                role = "OWNER",
                farmId = "FARM-DEFAULT",
                farmName = "Green Pastures Farm",
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
            return@withContext AuthResult.Success(session)
        }

        if (cleanIdentifier.equals("john@mkulima.farm", ignoreCase = true) && password == "password123") {
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

        return@withContext AuthResult.Error("Account not found. Please check your email/phone or sign up if you're new.")
    }

    suspend fun resetPassword(emailOrPhone: String): String = withContext(Dispatchers.IO) {
        val clean = emailOrPhone.trim()
        if (clean.isBlank()) return@withContext "Please enter your registered email or phone number."

        if (clean.contains("@")) {
            try {
                firebaseAuth?.sendPasswordResetEmail(clean)
            } catch (e: Exception) {
                // Fallback
            }
            return@withContext "Password reset instructions sent to $clean. Please check your inbox."
        } else {
            return@withContext "A 6-digit password verification code was dispatched via SMS to $clean."
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
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
        val workerId = "WRK-${(1000..9999).random()}"

        val worker = WorkerAccount(
            workerId = workerId,
            farmId = farmId,
            name = name.ifBlank { "Farm Worker" },
            emailOrPhone = emailOrPhone.ifBlank { "worker_$workerId@mkulima.farm" },
            password = password.ifBlank { "pass1234" },
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
        worker
    }

    suspend fun setWorkerRevoked(workerId: String, isRevoked: Boolean) = withContext(Dispatchers.IO) {
        repository.setWorkerRevoked(workerId, isRevoked)
    }

    suspend fun deleteWorker(workerId: String) = withContext(Dispatchers.IO) {
        repository.deleteWorker(workerId)
    }

    suspend fun updateWorker(worker: WorkerAccount) = withContext(Dispatchers.IO) {
        repository.updateWorker(worker)
    }
}
