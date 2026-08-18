package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthResult
import kotlinx.coroutines.flow.Flow
import com.example.data.CattleEvent
import com.example.data.EggLog
import com.example.data.EmployeeRequest
import com.example.data.FarmRepository
import com.example.data.FarmSettings
import com.example.data.FarmTask
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.RequestStatus
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import com.example.data.UserSession
import com.example.data.WorkerAccount
import com.example.data.WorkerPermissions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TaskStatusFilter { ALL, PENDING, COMPLETED, HIGH_PRIORITY }

@OptIn(ExperimentalCoroutinesApi::class)
class FarmViewModel(
    private val repository: FarmRepository,
    val authManager: AuthManager
) : ViewModel() {

    val currentSession: StateFlow<UserSession?> = authManager.currentSession

    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<TaskCategory?>(null)
    val selectedStatusFilter = MutableStateFlow(TaskStatusFilter.ALL)

    // Farm Scoped Streams
    val allUnits: StateFlow<List<FarmUnit>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getUnitsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allMilkLogs: StateFlow<List<MilkLog>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getMilkLogsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allEggLogs: StateFlow<List<EggLog>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getEggLogsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFinanceRecords: StateFlow<List<FinanceRecord>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getFinanceRecordsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allEmployeeRequests: StateFlow<List<EmployeeRequest>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        if (session != null && session.role.equals("WORKER", ignoreCase = true)) {
            repository.getEmployeeRequestsForWorker(
                farmId = farmId,
                workerId = session.userId,
                emailOrPhone = session.emailOrPhone,
                name = session.name
            )
        } else {
            repository.getEmployeeRequestsForFarm(farmId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val farmSettings: StateFlow<FarmSettings> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getSettingsForFarm(farmId).map { it ?: FarmSettings(farmId = farmId) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FarmSettings()
    )

    val farmWorkers: StateFlow<List<WorkerAccount>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getWorkersForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSettings(settings: FarmSettings) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.updateSettings(settings.copy(farmId = farmId))
        }
    }

    val rawTasks: StateFlow<List<FarmTask>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getTasksForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredTasks: StateFlow<List<FarmTask>> = combine(
        rawTasks,
        searchQuery,
        selectedCategoryFilter,
        selectedStatusFilter
    ) { tasks, query, categoryFilter, statusFilter ->
        tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.targetUnit.contains(query, ignoreCase = true) ||
                    (task.instructions?.contains(query, ignoreCase = true) == true)

            val matchesCategory = categoryFilter == null || task.category == categoryFilter

            val matchesStatus = when (statusFilter) {
                TaskStatusFilter.ALL -> true
                TaskStatusFilter.PENDING -> !task.isCompleted
                TaskStatusFilter.COMPLETED -> task.isCompleted
                TaskStatusFilter.HIGH_PRIORITY -> task.priority == TaskPriority.HIGH
            }

            matchesQuery && matchesCategory && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Auth Actions
    fun login(emailOrPhone: String, pass: String, onError: (String) -> Unit, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = authManager.login(emailOrPhone, pass)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error -> onError(result.message)
            }
        }
    }

    fun signUpOwner(name: String, emailOrPhone: String, pass: String, farmName: String, onError: (String) -> Unit, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = authManager.signUpOwner(name, emailOrPhone, pass, farmName)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error -> onError(result.message)
            }
        }
    }

    fun forgotPassword(emailOrPhone: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val response = authManager.resetPassword(emailOrPhone)
            onComplete(response)
        }
    }

    fun logout() {
        authManager.logout()
    }

    // Task CRUD
    suspend fun getTaskById(id: Long): FarmTask? = repository.getTaskById(id)
    fun addNewTask(title: String, category: TaskCategory, targetUnit: String, priority: TaskPriority, scheduledTime: String, instructions: String?, worker: String?) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.insertTask(FarmTask(farmId = farmId, title = title, category = category, targetUnit = targetUnit, priority = priority, scheduledTime = scheduledTime, instructions = instructions, assignedWorker = worker))
        }
    }

    fun markTaskComplete(id: Long, proofUri: String?, notes: String?) {
        viewModelScope.launch {
            val task = repository.getTaskById(id) ?: return@launch
            repository.updateTask(task.copy(isCompleted = true, completedAt = notes ?: "", proofPhotoUri = proofUri, proofNotes = notes))
        }
    }

    fun markTaskIncomplete(id: Long) {
        viewModelScope.launch {
            val task = repository.getTaskById(id) ?: return@launch
            repository.updateTask(task.copy(isCompleted = false, completedAt = null, proofPhotoUri = null, proofNotes = null))
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // Unit CRUD
    fun addNewUnit(
        name: String,
        type: String,
        headCount: Int,
        healthStatus: String,
        location: String,
        tagNumber: String?,
        breed: String?,
        dob: String?,
        weightAtBirth: Double?,
        currentWeight: Double?,
        sire: String?,
        dam: String?
    ) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.insertUnit(
                FarmUnit(
                    farmId = farmId,
                    name = name,
                    type = type,
                    headCount = headCount,
                    healthStatus = healthStatus,
                    location = location,
                    tagNumber = tagNumber,
                    breed = breed,
                    dob = dob,
                    weightAtBirth = weightAtBirth,
                    currentWeight = currentWeight,
                    sire = sire ?: "",
                    dam = dam ?: ""
                )
            )
        }
    }

    fun updateUnit(unit: FarmUnit) {
        viewModelScope.launch {
            repository.updateUnit(unit)
        }
    }

    fun deleteUnit(id: Long) {
        viewModelScope.launch {
            repository.deleteUnit(id)
        }
    }

    // Milk Logs
    fun addMilkLog(cowName: String, unitName: String, litres: Double, session: String, fat: Double, date: String, notes: String?) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.insertMilkLog(MilkLog(farmId = farmId, cowName = cowName, unitName = unitName, litres = litres, session = session, fat = fat, date = date, notes = notes))
        }
    }

    fun deleteMilkLog(id: Long) {
        viewModelScope.launch {
            repository.deleteMilkLog(id)
        }
    }

    // Egg Logs
    fun addEggLog(unitName: String, totalEggs: Int, damagedEggs: Int, grade: String, notes: String?) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.insertEggLog(EggLog(farmId = farmId, unitName = unitName, totalEggs = totalEggs, damagedEggs = damagedEggs, grade = grade, notes = notes))
        }
    }

    fun deleteEggLog(id: Long) {
        viewModelScope.launch {
            repository.deleteEggLog(id)
        }
    }

    // Finance
    fun addFinanceRecord(type: FinanceType, category: String, amount: Double, description: String) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.insertFinanceRecord(FinanceRecord(farmId = farmId, type = type, category = category, amount = amount, description = description, date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())))
        }
    }

    fun updateFinanceRecord(record: FinanceRecord) {
        viewModelScope.launch {
            repository.updateFinanceRecord(record)
        }
    }

    fun deleteFinanceRecord(recordId: Long) {
        viewModelScope.launch {
            repository.deleteFinanceRecord(recordId)
        }
    }

    // Employee Requests
    fun addEmployeeRequest(employeeName: String, requestType: String, amount: Double, startDate: String, endDate: String, reason: String) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val sessionName = currentSession.value?.name ?: employeeName
            val todayFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val req = EmployeeRequest(farmId = farmId, employeeName = sessionName.ifBlank { "Farm Worker" }, requestType = requestType, amount = amount, startDate = startDate, endDate = endDate, reason = reason, requestedAt = todayFormatted)
            repository.insertEmployeeRequest(req)
        }
    }

    fun updateEmployeeRequestStatus(request: EmployeeRequest, newStatus: RequestStatus) {
        viewModelScope.launch {
            val updated = request.copy(status = newStatus)
            repository.updateEmployeeRequest(updated)
        }
    }

    // ... other helpers (photo save, workers, cattle events) remain unchanged

    fun savePhotoToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val proofsDir = File(context.filesDir, "task_proofs").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val destFile = File(proofsDir, "PROOF_$timeStamp.jpg")

            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }

    fun deleteWorker(workerId: String) {
        viewModelScope.launch {
            repository.deleteWorker(workerId)
        }
    }

    fun toggleWorkerRevoked(workerId: String, isRevoked: Boolean) {
        viewModelScope.launch {
            repository.setWorkerRevoked(workerId, isRevoked)
        }
    }

    fun updateWorker(worker: WorkerAccount) {
        viewModelScope.launch {
            repository.updateWorker(worker)
        }
    }

    fun createWorker(name: String, emailOrPhone: String, password: String, permissions: WorkerPermissions) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val worker = WorkerAccount(workerId = java.util.UUID.randomUUID().toString(), farmId = farmId, name = name, emailOrPhone = emailOrPhone, password = password, canViewLivestock = permissions.canViewLivestock, canEditLivestock = permissions.canEditLivestock, canViewLogs = permissions.canViewLogs, canEditLogs = permissions.canEditLogs, canViewFinance = permissions.canViewFinance, canEditFinance = permissions.canEditFinance, canViewTasks = permissions.canViewTasks, canCompleteTasks = permissions.canCompleteTasks, canViewRequests = permissions.canViewRequests)
            repository.insertWorker(worker)
        }
    }

    // Cattle Events
    fun getCattleEventsFlow(unitId: Long): Flow<List<CattleEvent>> = repository.getCattleEventsForUnit(unitId)

    fun addCattleEvent(unitId: Long, category: String, title: String, date: String, details: String, notes: String?, metricValue: String?) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val event = CattleEvent(farmId = farmId, unitId = unitId, category = category, title = title, date = date, details = details, notes = notes, metricValue = metricValue)
            repository.insertCattleEvent(event)
        }
    }

    fun deleteCattleEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteCattleEvent(eventId)
        }
    }
}
