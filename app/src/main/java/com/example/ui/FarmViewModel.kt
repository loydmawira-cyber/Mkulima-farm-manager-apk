package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthResult
import com.example.auth.WorkerAccountCreationResult
import kotlinx.coroutines.flow.Flow
import com.example.data.CattleEvent
import com.example.data.EggLog
import com.example.data.EmployeeRequest
import com.example.data.FarmRepository
import com.example.data.FarmSettings
import com.example.data.FarmTask
import com.example.data.InventoryItem
import com.example.data.FieldPlan
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.PoultryLog
import com.example.data.ReminderCompletion
import com.example.data.RequestStatus
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import com.example.data.UserSession
import com.example.data.WorkerAccount
import com.example.data.WorkerPermissions
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
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

    val allPoultryLogs: StateFlow<List<PoultryLog>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getAllPoultryLogs(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allInventoryItems: StateFlow<List<InventoryItem>> = currentSession.flatMapLatest { session ->
        repository.getInventoryItemsForFarm(session?.farmId ?: "FARM-DEFAULT")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFieldPlans: StateFlow<List<FieldPlan>> = currentSession.flatMapLatest { session ->
        repository.getFieldPlansForFarm(session?.farmId ?: "FARM-DEFAULT")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    authManager.cacheThemeMode(settings.themeMode)
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
        withContext(Dispatchers.Default) {
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
                is AuthResult.AccountAlreadyExists -> onError(result.message)
            }
        }
    }

    fun signUpOwner(
        name: String,
        emailOrPhone: String,
        pass: String,
        farmName: String,
        countryCode: String = "+254",
        phoneNumber: String = "",
        onError: (String) -> Unit,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val result = authManager.signUpOwner(name, emailOrPhone, pass, farmName, countryCode, phoneNumber)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error -> onError(result.message)
                is AuthResult.AccountAlreadyExists -> onError(result.message)
            }
        }
    }

    fun checkPhoneNumberExists(countryCode: String, phoneNumber: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val formatted = AuthManager.formatPhoneNumber(countryCode, phoneNumber)
            val exists = authManager.checkPhoneNumberExists(formatted)
            onResult(exists, formatted)
        }
    }

    fun forgotPassword(emailOrPhone: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val response = authManager.resetPassword(emailOrPhone)
            onComplete(response)
        }
    }

    fun completePasswordReset(emailOrPhone: String, newPass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = authManager.completePasswordReset(emailOrPhone, newPass)
            onResult(success)
        }
    }

    fun logout() {
        authManager.logout()
    }

    // Task CRUD
    suspend fun getTaskById(id: Long): FarmTask? = repository.getTaskById(id)

    fun addNewTask(
        title: String,
        category: TaskCategory,
        targetUnit: String,
        priority: TaskPriority,
        scheduledTime: String,
        instructions: String?,
        assignedWorker: String?
    ) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val newTask = FarmTask(
                farmId = farmId,
                title = title.ifBlank { "Farm Maintenance Task" },
                category = category,
                targetUnit = targetUnit.ifBlank { "General Farm Area" },
                priority = priority,
                scheduledTime = scheduledTime.ifBlank { "Today" },
                instructions = instructions,
                assignedWorker = assignedWorker?.ifBlank { "Lead Operator" } ?: "Lead Operator"
            )
            repository.insertTask(newTask)
        }
    }

    fun completeReminderAsTask(
        title: String,
        targetUnit: String,
        dueDateStr: String,
        details: String?,
        sourceTaskId: Long? = null,
        reminderRuleKey: String? = null,
        reminderUnitId: Long? = null
    ) {
        viewModelScope.launch {
            if (sourceTaskId != null) {
                // Reminder was generated from a real task — update that task directly
                // instead of creating an orphaned duplicate.
                val existingTask = repository.getTaskById(sourceTaskId)
                if (existingTask != null) {
                    val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                    repository.updateTask(
                        existingTask.copy(
                            isCompleted = true,
                            completedAt = nowFormatted,
                            proofNotes = existingTask.proofNotes ?: "Completed from Reminders."
                        )
                    )
                    return@launch
                }
                // Fall through if the task was deleted out from under the reminder.
            }

            if (reminderRuleKey != null) {
                // Computed reminder (vaccination/deworming/etc.) — record completion so
                // it stays suppressed for its cooldown window instead of recreating a task.
                val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
                repository.markReminderComplete(farmId, reminderRuleKey, reminderUnitId ?: 0L)
                return@launch
            }

            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val newTask = FarmTask(
                farmId = farmId,
                title = title.ifBlank { "Farm Reminder" },
                category = TaskCategory.LIVESTOCK,
                targetUnit = targetUnit.ifBlank { "General Farm Area" },
                priority = TaskPriority.HIGH,
                scheduledTime = dueDateStr.ifBlank { "Today" },
                instructions = details,
                assignedWorker = "Lead Operator",
                isCompleted = true,
                completedAt = nowFormatted,
                proofNotes = "Auto-created from completed reminder."
            )
            repository.insertTask(newTask)
        }
    }

    /** Marks a computed reminder (including a poultry vaccination rule) complete.
     * The completion is stored in Room and therefore survives process death/restart.
     */
    fun markReminderComplete(ruleKey: String, unitId: Long) {
        if (ruleKey.isBlank()) return
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.markReminderComplete(farmId, ruleKey, unitId)
        }
    }

    /** Reopens a computed reminder by removing its persisted completion record. */
    fun clearReminderCompletion(ruleKey: String) {
        if (ruleKey.isBlank()) return
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.clearReminderCompletion(farmId, ruleKey)
        }
    }

    fun completeTaskWithProof(
        taskId: Long,
        photoUriString: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val existingTask = repository.getTaskById(taskId) ?: return@launch
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val updatedTask = existingTask.copy(
                isCompleted = true,
                completedAt = nowFormatted,
                proofPhotoUri = photoUriString ?: existingTask.proofPhotoUri,
                proofNotes = notes ?: existingTask.proofNotes ?: "Task completed with photo verification."
            )
            repository.updateTask(updatedTask)
        }
    }

    fun markTaskComplete(id: Long, proofUri: String?, notes: String?) {
        completeTaskWithProof(id, proofUri, notes)
    }

    fun markTaskIncomplete(taskId: Long) {
        viewModelScope.launch {
            val existingTask = repository.getTaskById(taskId) ?: return@launch
            val updatedTask = existingTask.copy(
                isCompleted = false,
                completedAt = null,
                proofPhotoUri = null,
                proofNotes = null
            )
            repository.updateTask(updatedTask)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    // Unit CRUD
    fun addNewUnit(
        name: String,
        type: String,
        headCount: Int,
        healthStatus: String,
        location: String,
        tagNumber: String = "",
        breed: String = "",
        dob: String = "",
        dateAdded: String = "",
        weightAtBirth: String = "",
        currentWeight: String = "",
        sire: String = "",
        dam: String = ""
    ) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val newUnit = FarmUnit(
                farmId = farmId,
                name = name.ifBlank { "Farm Unit" },
                type = type.ifBlank { "Cattle" },
                headCount = headCount,
                healthStatus = healthStatus.ifBlank { "Optimal" },
                location = location.ifBlank { "Main Sector" },
                lastUpdated = nowFormatted,
                tagNumber = tagNumber,
                breed = breed,
                dob = dob,
                dateAdded = if (dateAdded.isNotBlank()) dateAdded else dob,
                weightAtBirth = weightAtBirth,
                currentWeight = currentWeight,
                sire = sire,
                dam = dam
            )
            repository.insertUnit(newUnit)
        }
    }

    fun updateUnit(unit: FarmUnit) {
        viewModelScope.launch {
            repository.updateUnit(unit)
        }
    }

    fun updateUnitHeadCount(unitId: Long, newHeadCount: Int) {
        viewModelScope.launch {
            val existing = allUnits.value.find { it.id == unitId }
            if (existing != null) {
                val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                val updated = existing.copy(
                    headCount = newHeadCount.coerceAtLeast(0),
                    lastUpdated = nowFormatted
                )
                repository.updateUnit(updated)
            }
        }
    }

    fun updateUnitPhoto(unitId: Long, photoUri: String?) {
        viewModelScope.launch {
            val existing = allUnits.value.find { it.id == unitId }
            if (existing != null) {
                val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                val updated = existing.copy(
                    photoUri = photoUri,
                    lastUpdated = nowFormatted
                )
                repository.updateUnit(updated)
            }
        }
    }

    fun deleteUnit(unitId: Long) {
        viewModelScope.launch {
            repository.deleteUnit(unitId)
        }
    }

    // Milk Logs
    fun addMilkLog(
        cowName: String,
        unitName: String,
        litres: Double,
        session: String,
        fatPercentage: Double = 3.8,
        date: String = "",
        notes: String? = null
    ) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val todayFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val log = MilkLog(
                farmId = farmId,
                cowName = cowName.ifBlank { "Daisy (Friesian)" },
                unitName = unitName.ifBlank { "Dairy Herd - Friesians" },
                litres = litres,
                session = session,
                fatPercentage = fatPercentage,
                date = date.ifBlank { todayFormatted },
                loggedAt = nowFormatted,
                notes = notes
            )
            repository.insertMilkLog(log)
        }
    }

    fun deleteMilkLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteMilkLog(logId)
        }
    }

    // Egg Logs
    fun addEggLog(
        unitName: String,
        totalEggs: Int,
        damagedEggs: Int = 0,
        grade: String = "Grade A",
        notes: String? = null
    ) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val log = EggLog(
                farmId = farmId,
                unitName = unitName.ifBlank { "Poultry Flock" },
                totalEggs = totalEggs,
                damagedEggs = damagedEggs,
                grade = grade,
                loggedAt = nowFormatted,
                notes = notes
            )
            repository.insertEggLog(log)
        }
    }

    fun deleteEggLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteEggLog(logId)
        }
    }


    // ================= Assets: Inventory & Fields =================
    fun addInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val isSilage = item.isSilage || item.category.equals("Silage", ignoreCase = true)
            val prepared = item.copy(farmId = farmId, isSilage = isSilage, unitCost = if (isSilage) 0.0 else item.unitCost)
            repository.insertInventoryItem(prepared)
            val purchaseTotal = prepared.quantityAvailable * prepared.unitCost
            if (!isSilage && purchaseTotal > 0.0) {
                addFinanceRecord(FinanceType.EXPENSE, "Inventory Purchase", purchaseTotal,
                    "${prepared.itemName}: ${prepared.quantityAvailable} ${prepared.unitOfMeasurement}", prepared.purchaseDate)
            }
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch { repository.updateInventoryItem(item) }
    }

    fun addFieldPlan(field: FieldPlan) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.insertFieldPlan(field.copy(farmId = farmId))
        }
    }

    fun recordFieldHarvest(field: FieldPlan, outcome: String, tonnes: Double, saleAmount: Double, harvestDate: String) {
        viewModelScope.launch {
            if (tonnes <= 0.0 || field.status == "HARVESTED") return@launch
            val finalOutcome = outcome.uppercase()
            val updated = field.copy(status = "HARVESTED", harvestedDate = harvestDate, harvestOutcome = finalOutcome,
                harvestedTonnes = tonnes, saleAmount = if (finalOutcome == "SOLD") saleAmount else 0.0)
            repository.updateFieldPlan(updated)
            if (finalOutcome == "SILAGE") {
                repository.receiveSilage(updated.farmId, tonnes, updated.fieldName, harvestDate)
            } else if (finalOutcome == "SOLD" && saleAmount > 0.0) {
                addFinanceRecord(FinanceType.INCOME, "Crop Sale", saleAmount,
                    "${updated.cropName} harvest from ${updated.fieldName} (${tonnes} tonnes)", harvestDate)
            }
        }
    }

    // Finance
    fun addFinanceRecord(
        type: FinanceType,
        category: String,
        amount: Double,
        description: String,
        date: String = ""
    ) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val todayFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val record = FinanceRecord(
                farmId = farmId,
                type = type,
                category = category.ifBlank { "General" },
                amount = amount,
                date = if (date.isNotBlank()) date else todayFormatted,
                description = description.ifBlank { "Farm transaction" }
            )
            repository.insertFinanceRecord(record)
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

    fun deleteFinanceRecord(record: FinanceRecord) {
        deleteFinanceRecord(record.id)
    }

    // Employee Requests
    fun addEmployeeRequest(
        employeeName: String,
        requestType: String,
        amount: Double,
        startDate: String,
        endDate: String,
        reason: String
    ) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val sessionName = currentSession.value?.name ?: employeeName
            val todayFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val req = EmployeeRequest(
                farmId = farmId,
                employeeName = sessionName.ifBlank { "Farm Worker" },
                requestType = requestType,
                amount = amount,
                startDate = startDate,
                endDate = endDate,
                reason = reason.ifBlank { "No reason provided" },
                status = RequestStatus.PENDING,
                submittedAt = todayFormatted
            )
            repository.insertEmployeeRequest(req)
        }
    }

    fun updateEmployeeRequestStatus(request: EmployeeRequest, newStatus: RequestStatus) {
        viewModelScope.launch {
            val updated = request.copy(status = newStatus)
            repository.updateEmployeeRequest(updated)
        }
    }

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
            authManager.deleteWorker(workerId)
        }
    }

    fun toggleWorkerRevoked(workerId: String, isRevoked: Boolean) {
        viewModelScope.launch {
            authManager.setWorkerRevoked(workerId, isRevoked)
        }
    }

    fun updateWorker(worker: WorkerAccount) {
        viewModelScope.launch {
            authManager.updateWorker(worker)
        }
    }

    fun createWorker(
        name: String,
        emailOrPhone: String,
        password: String,
        permissions: WorkerPermissions,
        onResult: (WorkerAccountCreationResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            val session = currentSession.value
            if (session == null || !session.isOwner) {
                onResult(WorkerAccountCreationResult.Error("Only the farm owner can create a worker account."))
                return@launch
            }
            val result = authManager.createWorkerAccount(
                name = name,
                emailOrPhone = emailOrPhone,
                pass = password,
                permissions = permissions,
                farmId = session.farmId,
                farmName = session.farmName
            )
            onResult(result)
        }
    }

    // Cattle Events (Optimized as StateFlow)
    fun getCattleEventsFlow(unitId: Long): Flow<List<CattleEvent>> = repository.getCattleEventsForUnit(unitId)

    val allCattleEvents: StateFlow<List<CattleEvent>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getAllCattleEvents(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reminderCompletions: StateFlow<List<ReminderCompletion>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getReminderCompletionsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val farmReminders: StateFlow<List<com.example.util.FarmReminder>> = combine(
        allUnits,
        rawTasks,
        allCattleEvents,
        reminderCompletions
    ) { units, tasks, cattleEvents, completions ->
        withContext(Dispatchers.Default) {
            val eventsMap = cattleEvents.groupBy { it.unitId }.mapValues { entry ->
                entry.value.map {
                    com.example.ui.screens.CattleEventItem(
                        id = it.id.toString(),
                        category = it.category,
                        title = it.title,
                        date = it.date,
                        details = it.details,
                        notes = it.notes ?: "",
                        metricValue = it.metricValue ?: ""
                    )
                }
            }
            val completedRuleKeys = completions.associate { it.ruleKey to it.completedAt }
            com.example.util.FarmReminderEngine.computeAllReminders(
                units = units,
                cattleEventsMap = eventsMap,
                tasks = tasks,
                completedRuleKeys = completedRuleKeys
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addCattleEvent(unitId: Long, category: String, title: String, date: String, details: String, notes: String?, metricValue: String?) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val event = CattleEvent(farmId = farmId, unitId = unitId, category = category, title = title, date = date, details = details, notes = notes, metricValue = metricValue)
            repository.insertCattleEvent(event)

            // Auto-update animal status based on breeding event
            if (unitId > 0) {
                val existing = allUnits.value.find { it.id == unitId }
                if (existing != null) {
                    val isPositivePd = category.equals("PD", ignoreCase = true) && (
                        title.contains("Positive", ignoreCase = true) ||
                        details.contains("Positive", ignoreCase = true) ||
                        details.contains("Pregnant", ignoreCase = true) ||
                        details.contains("In-Calf", ignoreCase = true) ||
                        metricValue?.contains("Positive", ignoreCase = true) == true
                    )
                    val isNegativePd = category.equals("PD", ignoreCase = true) && (
                        title.contains("Negative", ignoreCase = true) ||
                        details.contains("Negative", ignoreCase = true) ||
                        metricValue?.contains("Negative", ignoreCase = true) == true
                    )
                    val isInsemination = category.equals("INSEMINATION", ignoreCase = true) || title.contains("Insemination", ignoreCase = true)
                    val isCalving = category.equals("CALVING", ignoreCase = true) || title.contains("Calving", ignoreCase = true)
                    val isDryOff = category.equals("DRY_OFF", ignoreCase = true) || title.contains("Dry Off", ignoreCase = true)

                    val hasCalvedOrMilking = existing.healthStatus.contains("Milking", ignoreCase = true) ||
                        existing.healthStatus.contains("Lactating", ignoreCase = true) ||
                        existing.healthStatus.contains("In-Calf / Milking", ignoreCase = true) ||
                        !existing.healthStatus.contains("Heifer", ignoreCase = true)

                    val newStatus = when {
                        isPositivePd -> if (hasCalvedOrMilking) "In-Calf / Milking" else "In-Calf"
                        isNegativePd -> if (hasCalvedOrMilking) "Milking (Open)" else "Heifer (Open)"
                        isInsemination -> "Inseminated"
                        isCalving -> "Milking"
                        isDryOff -> "Dry"
                        else -> null
                    }
                    if (newStatus != null) {
                        val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                        repository.updateUnit(existing.copy(healthStatus = newStatus, lastUpdated = nowFormatted))
                    }
                }
            }
        }
    }

    fun updateCattleEvent(eventId: Long, unitId: Long, category: String, title: String, date: String, details: String, notes: String?, metricValue: String?) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val event = CattleEvent(id = eventId, farmId = farmId, unitId = unitId, category = category, title = title, date = date, details = details, notes = notes, metricValue = metricValue)
            repository.updateCattleEvent(event)
        }
    }

    fun deleteCattleEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteCattleEvent(eventId)
        }
    }


    // Poultry Logs
    fun getPoultryLogsFlow(unitId: Long): Flow<List<PoultryLog>> = repository.getPoultryLogsForUnit(unitId)

    fun addPoultryLog(log: PoultryLog) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.insertPoultryLog(log.copy(farmId = farmId))
        }
    }

    fun updatePoultryLog(log: PoultryLog) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.updatePoultryLog(log.copy(farmId = farmId))
        }
    }

    fun deletePoultryLog(logId: Long) {
        viewModelScope.launch {
            repository.deletePoultryLog(logId)
        }
    }

    fun clearCurrentFarmData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.clearFarmData(farmId)
            onComplete()
        }
    }

    fun clearAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllData()
            onComplete()
        }
    }
}
