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
import com.example.data.FeedPlan
import com.example.data.FarmTask
import com.example.data.InventoryItem
import com.example.data.InventoryMovement
import com.example.data.FieldPlan
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.MilkLogEntryRules
import com.example.data.MilkUsageLog
import com.example.data.MonthlyReport
import com.example.data.PoultryLog
import com.example.data.ReminderCompletion
import com.example.data.RequestStatus
import com.example.data.SyncStatus
import com.example.data.FarmSubscriptionAccess
import com.example.data.SubscriptionPolicy
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TaskStatusFilter { ALL, PENDING, COMPLETED, HIGH_PRIORITY }

@OptIn(ExperimentalCoroutinesApi::class)
class FarmViewModel(
    private val repository: FarmRepository,
    val authManager: AuthManager
) : ViewModel() {

    val currentSession: StateFlow<UserSession?> = authManager.currentSession

    // Added StateFlow for observing sync status in UI:
    val syncStatus: StateFlow<SyncStatus> = repository.syncEngine?.syncStatus
        ?: MutableStateFlow(SyncStatus.Offline)

    // Added manual sync trigger function:
    fun triggerManualSync() {
        val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
        repository.syncEngine?.triggerPush(farmId)
    }

    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<TaskCategory?>(null)
    // Daily Task Operations should show active work by default; completed history remains available via Completed.
    val selectedStatusFilter = MutableStateFlow(TaskStatusFilter.PENDING)

    // Farm Scoped Streams
    val allUnits: StateFlow<List<FarmUnit>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getUnitsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val allMilkLogs: StateFlow<List<MilkLog>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getMilkLogsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val allMilkUsageLogs: StateFlow<List<MilkUsageLog>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getMilkUsageLogsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val allEggLogs: StateFlow<List<EggLog>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getEggLogsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val allPoultryLogs: StateFlow<List<PoultryLog>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getAllPoultryLogs(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val allInventoryItems: StateFlow<List<InventoryItem>> = currentSession.flatMapLatest { session ->
        repository.getInventoryItemsForFarm(session?.farmId ?: "FARM-DEFAULT")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allFieldPlans: StateFlow<List<FieldPlan>> = currentSession.flatMapLatest { session ->
        repository.getFieldPlansForFarm(session?.farmId ?: "FARM-DEFAULT")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allFeedPlans: StateFlow<List<FeedPlan>> = currentSession.flatMapLatest { session ->
        repository.getFeedPlansForFarm(session?.farmId ?: "FARM-DEFAULT")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allInventoryMovements: StateFlow<List<InventoryMovement>> = currentSession.flatMapLatest { session ->
        repository.getInventoryMovementsForFarm(session?.farmId ?: "FARM-DEFAULT")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allFinanceRecords: StateFlow<List<FinanceRecord>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getFinanceRecordsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val allMonthlyReports: StateFlow<List<MonthlyReport>> = currentSession.flatMapLatest { session ->
        repository.getMonthlyReportsForFarm(session?.farmId ?: "FARM-DEFAULT")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
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
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val farmSettings: StateFlow<FarmSettings> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getSettingsForFarm(farmId).map { it ?: FarmSettings(farmId = farmId) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FarmSettings()
    )

    val subscriptionAccess: StateFlow<FarmSubscriptionAccess> = farmSettings
        .map { settings ->
            SubscriptionPolicy.accessFor(
                tierName = settings.subscriptionTier,
                statusName = settings.subscriptionStatus,
                expiresAt = settings.subscriptionExpiresAt
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SubscriptionPolicy.accessFor("FREE", "ACTIVE", 0L)
        )

    fun canWriteFarmData(): Boolean = !subscriptionAccess.value.isReadOnly

    fun activateSubscription(
        tier: String,
        expiresAt: Long = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000L,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            val current = farmSettings.value
            val updated = current.copy(
                farmId = farmId,
                subscriptionTier = tier.uppercase(),
                subscriptionStatus = "ACTIVE",
                subscriptionExpiresAt = expiresAt,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateSettings(updated)
            onComplete?.invoke()
        }
    }

    val farmWorkers: StateFlow<List<WorkerAccount>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getWorkersForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun updateSettings(settings: FarmSettings) {
    authManager.cacheThemeMode(settings.themeMode)
    viewModelScope.launch {
        val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
        repository.updateSettings(settings.copy(farmId = farmId))
    }
}

    fun updateFarmName(farmName: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authManager.updateFarmName(farmName)
                .onSuccess(onSuccess)
                .onFailure { onError(it.message ?: "Unable to update the farm name.") }
        }
    }

    fun updateRecoveryEmail(email: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authManager.updateRecoveryEmail(email)
                .onSuccess(onSuccess)
                .onFailure { onError(it.message ?: "Unable to update the recovery email.") }
        }
    }

    val rawTasks: StateFlow<List<FarmTask>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getTasksForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
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
        started = SharingStarted.Eagerly,
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
            if (!canWriteFarmData()) return@launch
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
            if (!canWriteFarmData()) return@launch
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
            if (!canWriteFarmData()) return@launch
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.markReminderComplete(farmId, ruleKey, unitId)
        }
    }

    /** Reopens a computed reminder by removing its persisted completion record. */
    fun clearReminderCompletion(ruleKey: String) {
        if (ruleKey.isBlank()) return
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.clearReminderCompletion(farmId, ruleKey)
        }
    }

    private fun findMatchingUnitForTask(task: FarmTask, units: List<FarmUnit>): FarmUnit? {
        val syncIdParts = task.syncId.split("-")
        val unitIdFromSync = syncIdParts.lastOrNull()?.toLongOrNull()
        if (unitIdFromSync != null && unitIdFromSync > 0) {
            val direct = units.find { it.id == unitIdFromSync }
            if (direct != null) return direct
        }
        val targetName = task.targetUnit.trim()
        if (targetName.isNotBlank() && targetName != "General Farm Area" && targetName != "General Farm Task") {
            val directMatch = units.find {
                it.name.equals(targetName, ignoreCase = true) ||
                    "${it.name} • Tag ${it.tagNumber}".equals(targetName, ignoreCase = true) ||
                    (it.tagNumber.isNotBlank() && targetName.contains(it.tagNumber, ignoreCase = true)) ||
                    targetName.contains(it.name, ignoreCase = true)
            }
            if (directMatch != null) return directMatch
        }
        return units.find { task.title.contains(it.name, ignoreCase = true) }
    }

    fun completeTaskWithProof(
        taskId: Long,
        photoUriString: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            val existingTask = repository.getTaskById(taskId) ?: return@launch
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val updatedTask = existingTask.copy(
                isCompleted = true,
                completedAt = nowFormatted,
                proofPhotoUri = photoUriString ?: existingTask.proofPhotoUri,
                proofNotes = notes ?: existingTask.proofNotes ?: "Task completed with photo verification.",
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)

            val farmId = existingTask.farmId.ifBlank { currentSession.value?.farmId ?: "FARM-DEFAULT" }
            val isDeworm = existingTask.title.contains("Deworm", ignoreCase = true) ||
                existingTask.syncId.contains("deworm", ignoreCase = true) ||
                (existingTask.instructions?.contains("deworm", ignoreCase = true) == true)
            val isVaccin = existingTask.title.contains("Vaccin", ignoreCase = true) ||
                existingTask.syncId.contains("vac", ignoreCase = true) ||
                (existingTask.instructions?.contains("vaccin", ignoreCase = true) == true)

            val matchedUnit = findMatchingUnitForTask(existingTask, allUnits.value)
            if (matchedUnit != null) {
                if (isCattleUnit(matchedUnit)) {
                    if (isDeworm) {
                        val event = CattleEvent(
                            farmId = farmId,
                            unitId = matchedUnit.id,
                            category = "DEWORMING",
                            title = "Deworming Administered",
                            date = dateFormatted,
                            details = existingTask.instructions ?: "Routine deworming completed from Farm Tasks & Operations",
                            notes = notes ?: "Completed on $nowFormatted"
                        )
                        repository.insertCattleEvent(event)
                        repository.markReminderComplete(farmId, "cattle_deworm_${matchedUnit.id}", matchedUnit.id)
                        repository.markReminderComplete(farmId, "cattle_deworm_routine_${matchedUnit.id}", matchedUnit.id)

                        if (matchedUnit.healthStatus.contains("Deworm", ignoreCase = true)) {
                            repository.updateUnit(matchedUnit.copy(healthStatus = "Healthy", lastUpdated = nowFormatted))
                        }
                        synchronizeDewormingTask(
                            matchedUnit,
                            sourceEvents = allCattleEvents.value.filterNot { it.id == event.id } + event
                        )
                    } else if (isVaccin) {
                        val event = CattleEvent(
                            farmId = farmId,
                            unitId = matchedUnit.id,
                            category = "VACCINATION",
                            title = existingTask.title,
                            date = dateFormatted,
                            details = existingTask.instructions ?: "Vaccination administered from Tasks",
                            notes = notes ?: "Completed on $nowFormatted"
                        )
                        repository.insertCattleEvent(event)
                        repository.markReminderComplete(farmId, "cattle_vac_${matchedUnit.id}", matchedUnit.id)
                        repository.markReminderComplete(farmId, "cattle_vac_routine_${matchedUnit.id}", matchedUnit.id)
                        if (matchedUnit.healthStatus.contains("Vaccin", ignoreCase = true)) {
                            repository.updateUnit(matchedUnit.copy(healthStatus = "Healthy", lastUpdated = nowFormatted))
                        }
                    }
                } else {
                    // Poultry unit
                    if (isDeworm) {
                        val pLog = PoultryLog(
                            farmId = farmId,
                            unitId = matchedUnit.id,
                            logType = "VACCINATION",
                            vaccineName = "Routine Deworming",
                            targetStage = "Flock Deworming",
                            vaccineStatus = "COMPLETED",
                            date = dateFormatted,
                            notes = notes ?: "Flock deworming completed"
                        )
                        repository.insertPoultryLog(pLog)
                        repository.markReminderComplete(farmId, "poultry_deworm_${matchedUnit.id}", matchedUnit.id)
                        if (matchedUnit.healthStatus.contains("Deworm", ignoreCase = true)) {
                            repository.updateUnit(matchedUnit.copy(healthStatus = "Optimal", lastUpdated = nowFormatted))
                        }
                    } else if (isVaccin) {
                        val pLog = PoultryLog(
                            farmId = farmId,
                            unitId = matchedUnit.id,
                            logType = "VACCINATION",
                            vaccineName = existingTask.title,
                            targetStage = "Scheduled Vaccine",
                            vaccineStatus = "COMPLETED",
                            date = dateFormatted,
                            notes = notes ?: "Vaccine completed"
                        )
                        repository.insertPoultryLog(pLog)
                        repository.markReminderComplete(farmId, "poultry_vac_${matchedUnit.id}_${existingTask.id}", matchedUnit.id)
                        if (matchedUnit.healthStatus.contains("Vaccin", ignoreCase = true)) {
                            repository.updateUnit(matchedUnit.copy(healthStatus = "Optimal", lastUpdated = nowFormatted))
                        }
                    }
                }
            }
        }
    }

    fun markTaskComplete(id: Long, proofUri: String?, notes: String?) {
        completeTaskWithProof(id, proofUri, notes)
    }

    fun markTaskIncomplete(taskId: Long) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
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
            if (!canWriteFarmData()) return@launch
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
        dam: String = "",
        notes: String = "",
        onCreated: (FarmUnit) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val access = subscriptionAccess.value
            if (access.isReadOnly) {
                onError("Your ${access.tier.name.lowercase()} subscription has expired. Farm records are read-only until the owner renews.")
                return@launch
            }
            val isCattle = type.contains("cattle", ignoreCase = true) || type.contains("cow", ignoreCase = true)
            val isPoultry = type.contains("poultry", ignoreCase = true) || type.contains("flock", ignoreCase = true)
            val activeUnits = allUnits.value.filterNot { it.isDeleted }
            if (isCattle && activeUnits.count { isCattleUnit(it) } >= access.maxCattle) {
                onError("${access.tier.name.lowercase().replaceFirstChar { it.uppercase() }} plan allows up to ${access.maxCattle} cattle. Upgrade to add another cow.")
                return@launch
            }
            if (isPoultry && activeUnits.count { it.type.contains("poultry", ignoreCase = true) } >= access.maxPoultryFlocks) {
                val message = if (access.maxPoultryFlocks == 0) {
                    "Poultry entry is not included in the Free plan. Upgrade to Premium or Pro."
                } else {
                    "${access.tier.name.lowercase().replaceFirstChar { it.uppercase() }} plan allows up to ${access.maxPoultryFlocks} poultry flocks. Upgrade to add another flock."
                }
                onError(message)
                return@launch
            }
            runCatching {
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
                    dam = dam,
                    notes = notes.trim()
                )
                val savedUnit = withContext(Dispatchers.IO) { repository.insertUnitAndReturnPrepared(newUnit) }
                if (isCattleUnit(savedUnit)) synchronizeDewormingTask(savedUnit)
                savedUnit
            }.onSuccess(onCreated).onFailure { error ->
                onError(error.message ?: "Unable to save the animal. Please try again.")
            }
        }
    }

    fun updateUnit(unit: FarmUnit) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            val previousDob = allUnits.value.find { it.id == unit.id }?.dob
            repository.updateUnit(unit)
            if (isCattleUnit(unit) && previousDob != unit.dob) {
                synchronizeDewormingTask(unit)
            }
        }
    }

    fun updateUnitHeadCount(unitId: Long, newHeadCount: Int) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
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
            if (!canWriteFarmData()) return@launch
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
            if (!canWriteFarmData()) return@launch
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
        notes: String? = null,
        onRecorded: (MilkLog) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (!canWriteFarmData()) {
                onError("Subscription expired. Production records are read-only until the owner renews.")
                return@launch
            }
            runCatching {
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
                withContext(Dispatchers.IO) { repository.insertMilkLogAndReturn(log) }
            }.onSuccess(onRecorded).onFailure { error ->
                onError(error.message ?: "Unable to record milk. Please try again.")
            }
        }
    }

    fun deleteMilkLog(logId: Long) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            repository.deleteMilkLog(logId)
        }
    }

    fun saveMilkUsageLog(
        date: String,
        session: String,
        litresToCooperative: Double,
        litresHomeUse: Double,
        litresToCalves: Double,
        onSaved: (MilkUsageLog) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (!canWriteFarmData()) {
                onError("Subscription expired. Production records are read-only until the owner renews.")
                return@launch
            }
            runCatching {
                val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
                val todayFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                val usage = MilkUsageLog(
                    farmId = farmId,
                    date = date.ifBlank { todayFormatted },
                    session = MilkLogEntryRules.normalizedSession(session),
                    litresToCooperative = litresToCooperative,
                    litresHomeUse = litresHomeUse,
                    litresToCalves = litresToCalves
                )
                withContext(Dispatchers.IO) { repository.saveMilkUsageLog(usage) }
            }.onSuccess(onSaved).onFailure { error ->
                onError(error.message ?: "Unable to save milk usage. Please try again.")
            }
        }
    }

    fun deleteMilkUsageLog(id: Long) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            repository.deleteMilkUsageLog(id)
        }
    }

    fun editMilkUsageLog(
        id: Long,
        litresToCooperative: Double,
        litresHomeUse: Double,
        litresToCalves: Double,
        onSaved: (MilkUsageLog) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (!canWriteFarmData()) {
                onError("Subscription expired. Production records are read-only until the owner renews.")
                return@launch
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.editMilkUsageLog(id, litresToCooperative, litresHomeUse, litresToCalves)
                }
            }.onSuccess(onSaved).onFailure { error ->
                onError(error.message ?: "Unable to update milk usage. Please try again.")
            }
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
            if (!canWriteFarmData()) return@launch
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
            if (!canWriteFarmData()) return@launch
            repository.deleteEggLog(logId)
        }
    }


    // ================= Assets: Inventory & Fields =================
    fun addInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
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
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            repository.updateInventoryItem(item)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            repository.deleteInventoryItem(item.id)
        }
    }

    fun addFieldPlan(field: FieldPlan) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
            repository.insertFieldPlan(field.copy(farmId = farmId))
        }
    }

    fun updateFieldPlan(field: FieldPlan) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            repository.updateFieldPlan(field)
        }
    }

    fun deleteFieldPlan(field: FieldPlan) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
            repository.deleteFieldPlan(field.id)
        }
    }

    fun recordFieldHarvest(field: FieldPlan, outcome: String, tonnes: Double, saleAmount: Double, harvestDate: String) {
        viewModelScope.launch {
            if (!canWriteFarmData()) return@launch
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
            if (!subscriptionAccess.value.canUseFinance) return@launch
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
            if (!subscriptionAccess.value.canUseFinance) return@launch
            repository.updateFinanceRecord(record)
        }
    }

    fun deleteFinanceRecord(recordId: Long) {
        viewModelScope.launch {
            if (!subscriptionAccess.value.canUseFinance) return@launch
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
            if (!canWriteFarmData()) return@launch
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
            if (!canWriteFarmData()) return@launch
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
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val reminderCompletions: StateFlow<List<ReminderCompletion>> = currentSession.flatMapLatest { session ->
        val farmId = session?.farmId ?: "FARM-DEFAULT"
        repository.getReminderCompletionsForFarm(farmId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private var lastDewormingBootstrapKey: String = ""

    init {
        // Existing cattle pre-date this feature, so build their first task after
        // Room emits livestock and event data. The input key prevents duplicate
        // task rewrites when unrelated recompositions occur.
        viewModelScope.launch {
            combine(allUnits, allCattleEvents) { units, events -> units to events }
                .collect { (units, events) ->
                    val session = currentSession.value ?: return@collect
                    val cattle = units.filter(::isCattleUnit)
                    if (cattle.isEmpty()) return@collect

                    val sourceKey = buildString {
                        append(session.farmId)
                        cattle.sortedBy { it.id }.forEach { cow ->
                            append("|${cow.id}:${cow.dob}:${cow.updatedAt}")
                        }
                        events.filter { isDewormingEvent(it.category, it.title, it.details) }
                            .sortedBy { it.id }
                            .forEach { event ->
                                append("|${event.id}:${event.date}:${event.updatedAt}:${event.isDeleted}")
                            }
                    }
                    if (sourceKey == lastDewormingBootstrapKey) return@collect
                    lastDewormingBootstrapKey = sourceKey

                    cattle.forEach { cow ->
                        synchronizeDewormingTask(cow, sourceEvents = events)
                    }
                }
        }
    }

    /**
     * Future scheduled tasks must not be presented as due today. Unknown labels
     * remain in the due list because their date cannot be safely classified.
     */
    private fun isDueTodayOrEarlier(dueDateStr: String): Boolean {
        val raw = dueDateStr.trim()
        if (raw.isBlank() || raw.equals("today", ignoreCase = true) || raw.contains("overdue", ignoreCase = true)) {
            return true
        }
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val parsed = listOf("dd MMM yyyy", "dd MMM, yyyy", "yyyy-MM-dd", "dd/MM/yyyy")
            .firstNotNullOfOrNull { pattern ->
                runCatching {
                    SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }.parse(raw)
                }.getOrNull()
        }
        return parsed?.let { !it.after(todayStart) } ?: true
    }

    private fun reminderDateOrNull(dueDateStr: String): Date? {
        val raw = dueDateStr.trim()
        if (raw.isBlank() || raw.equals("today", ignoreCase = true) || raw.contains("overdue", ignoreCase = true)) {
            return null
        }
        return listOf("dd MMM yyyy", "dd MMM, yyyy", "yyyy-MM-dd", "dd/MM/yyyy")
            .firstNotNullOfOrNull { pattern ->
                runCatching {
                    SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }.parse(raw)
                }.getOrNull()
            }
    }

    /**
     * Due reminders stay first. If none are due, show the next few dated future
     * reminders rather than leaving the notification bell and dialog empty.
     */
    private fun selectVisibleReminders(
        reminders: List<com.example.util.FarmReminder>
    ): List<com.example.util.FarmReminder> {
        val dueNow = reminders.filter { reminder -> isDueTodayOrEarlier(reminder.dueDateStr) }
        if (dueNow.isNotEmpty()) return dueNow

        val todayStartMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return reminders
            .mapNotNull { reminder ->
                reminderDateOrNull(reminder.dueDateStr)?.let { dueDate -> reminder to dueDate.time }
            }
            .sortedBy { (_, dueAt) -> dueAt }
            .take(5)
            .map { (reminder, dueAt) ->
                reminder.copy(
                    urgency = com.example.util.ReminderUrgency.UPCOMING,
                    daysRemaining = ((dueAt - todayStartMillis) / 86_400_000L).toInt().coerceAtLeast(1)
                )
            }
    }

    val farmReminders: StateFlow<List<com.example.util.FarmReminder>> = combine(
        allUnits,
        rawTasks,
        allCattleEvents,
        reminderCompletions,
        subscriptionAccess
    ) { units, tasks, cattleEvents, completions, access ->
        withContext(Dispatchers.Default) {
            if (!access.canReceiveReminders) return@withContext emptyList()
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
            selectVisibleReminders(com.example.util.FarmReminderEngine.computeAllReminders(
                units = units,
                cattleEventsMap = eventsMap,
                tasks = tasks,
                completedRuleKeys = completedRuleKeys
            ))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
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

                    synchronizeRepeatHeatCheckTasks(event, existing)
                    if (isDewormingEvent(event.category, event.title, event.details)) {
                        synchronizeDewormingTask(
                            existing,
                            sourceEvents = allCattleEvents.value.filterNot { it.id == event.id } + event
                        )
                    }
                }
            }
        }
    }

    fun updateCattleEvent(eventId: Long, unitId: Long, category: String, title: String, date: String, details: String, notes: String?, metricValue: String?) {
        viewModelScope.launch {
            val existingEvent = repository.getCattleEventById(eventId) ?: return@launch
            val event = existingEvent.copy(
                unitId = unitId,
                category = category,
                title = title,
                date = date,
                details = details,
                notes = notes,
                metricValue = metricValue
            )
            repository.updateCattleEvent(event)
            val cow = allUnits.value.find { it.id == unitId }
            if (cow != null) {
                synchronizeRepeatHeatCheckTasks(event, cow)
                if (isDewormingEvent(event.category, event.title, event.details) ||
                    isDewormingEvent(existingEvent.category, existingEvent.title, existingEvent.details)
                ) {
                    synchronizeDewormingTask(
                        cow,
                        sourceEvents = allCattleEvents.value.filterNot { it.id == event.id } + event
                    )
                }
            }
        }
    }

    fun deleteCattleEvent(eventId: Long) {
        viewModelScope.launch {
            val event = repository.getCattleEventById(eventId)
            repository.deleteCattleEvent(eventId)
            if (event != null) {
                repository.softDeleteTasksBySyncIdPrefix("repeat-heat-${event.syncId}-day-", event.farmId)
                if (isDewormingEvent(event.category, event.title, event.details)) {
                    allUnits.value.find { it.id == event.unitId }?.let { cow ->
                        synchronizeDewormingTask(
                            cow,
                            sourceEvents = allCattleEvents.value.filterNot { it.id == event.id }
                        )
                    }
                }
            }
        }
    }

    private fun isRecordedHeatEvent(category: String, title: String, details: String): Boolean {
        val combinedText = "$category $title $details"
        return category.equals("HEAT", ignoreCase = true) ||
            category.equals("ESTRUS", ignoreCase = true) ||
            combinedText.contains("on heat", ignoreCase = true) ||
            combinedText.contains("heat observed", ignoreCase = true) ||
            combinedText.contains("estrus", ignoreCase = true)
    }

    private fun isCattleUnit(unit: FarmUnit): Boolean =
        !unit.type.contains("poultry", ignoreCase = true)

    private fun isDewormingEvent(category: String, title: String, details: String): Boolean {
        val combinedText = "$category $title $details"
        return category.equals("DEWORMING", ignoreCase = true) ||
            category.equals("DEWORM", ignoreCase = true) ||
            combinedText.contains("deworm", ignoreCase = true)
    }

    private fun parseFarmDate(rawValue: String): Date? {
        val raw = rawValue.trim()
        if (raw.isBlank()) return null
        return listOf("dd MMM yyyy", "dd MMM, yyyy", "yyyy-MM-dd", "dd/MM/yyyy")
            .firstNotNullOfOrNull { pattern ->
                runCatching {
                    SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }.parse(raw)
                }.getOrNull()
            }
    }

    private fun Date.plusCalendarAmount(field: Int, amount: Int): Date =
        Calendar.getInstance().apply {
            time = this@plusCalendarAmount
            add(field, amount)
        }.time

    /**
     * A calf starts with a first due date 30 days after birth. Monthly intervals
     * continue through the six-month date; the next completed deworming at or
     * after that threshold begins the three-month adult cycle.
     */
    private fun calculateNextDewormingDate(cow: FarmUnit, latestDeworming: CattleEvent?): Date {
        val birthDate = parseFarmDate(cow.dob)
        if (latestDeworming == null) {
            val firstCalfDueDate = birthDate?.plusCalendarAmount(Calendar.DAY_OF_YEAR, 30)
            return if (firstCalfDueDate != null && firstCalfDueDate.after(Date())) firstCalfDueDate else Date()
        }

        val lastDate = parseFarmDate(latestDeworming.date) ?: return Date()
        val sixMonthDate = birthDate?.plusCalendarAmount(Calendar.MONTH, 6)
        val monthlyCandidate = lastDate.plusCalendarAmount(Calendar.MONTH, 1)
        return if (sixMonthDate != null && !monthlyCandidate.after(sixMonthDate)) {
            monthlyCandidate
        } else {
            lastDate.plusCalendarAmount(Calendar.MONTH, 3)
        }
    }

    private fun latestDewormingEvent(events: List<CattleEvent>): CattleEvent? =
        events.filter { isDewormingEvent(it.category, it.title, it.details) }
            .maxByOrNull { parseFarmDate(it.date)?.time ?: 0L }

    private suspend fun synchronizeDewormingTask(
        cow: FarmUnit,
        sourceEvents: List<CattleEvent> = allCattleEvents.value
    ) {
        if (!isCattleUnit(cow)) return

        val targetName = if (cow.tagNumber.isBlank()) cow.name else "${cow.name} • Tag ${cow.tagNumber}"
        val taskSyncId = "cattle-deworming-${cow.farmId}-${cow.id}"

        val candidateTasks = repository.getTaskSnapshotForFarm(cow.farmId)
            .filter { task ->
                task.syncId == taskSyncId ||
                    task.targetUnit.equals(targetName, ignoreCase = true)
            }
        val existingTask = candidateTasks.firstOrNull { it.syncId == taskSyncId }
            ?: candidateTasks.firstOrNull { it.isCompleted }
            ?: candidateTasks.firstOrNull()

        var resolvedEvents = sourceEvents
        var latestDeworming = latestDewormingEvent(resolvedEvents.filter { it.unitId == cow.id })

        if (latestDeworming == null && existingTask != null && existingTask.isCompleted) {
            // `sourceEvents` can legitimately be an empty/stale snapshot right after
            // app launch — feeding StateFlows here use `initialValue = emptyList()`
            // (and now `SharingStarted.Eagerly`, which starts them immediately on
            // ViewModel creation, before Room/Firestore have necessarily emitted real
            // data). Treating an empty snapshot as "no event exists" caused a brand
            // new "Deworming Administered" event to be inserted on every cold start
            // for any cow with a previously-completed task. Re-check the DB directly —
            // bypassing the possibly-stale flow snapshot — before backfilling.
            val dbEventsForCow = repository.getCattleEventsForUnit(cow.id).first()
            val confirmedLatestDeworming = latestDewormingEvent(dbEventsForCow)

            if (confirmedLatestDeworming == null) {
                val completedDateStr = existingTask.completedAt?.let { parseFarmDate(it.substringBefore(",")) }?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) }
                    ?: parseFarmDate(existingTask.scheduledTime)?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) }
                    ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                val healEvent = CattleEvent(
                    farmId = cow.farmId,
                    unitId = cow.id,
                    category = "DEWORMING",
                    title = "Deworming Administered",
                    date = completedDateStr,
                    details = existingTask.instructions ?: "Routine deworming completed",
                    notes = existingTask.proofNotes ?: "Recorded from completed deworming task"
                )
                repository.insertCattleEvent(healEvent)
                repository.markReminderComplete(cow.farmId, "cattle_deworm_${cow.id}", cow.id)
                repository.markReminderComplete(cow.farmId, "cattle_deworm_routine_${cow.id}", cow.id)
                resolvedEvents = sourceEvents.filterNot { it.id == healEvent.id } + healEvent
                latestDeworming = healEvent
            } else {
                // A real event already exists in the DB; the in-memory sourceEvents
                // snapshot was just stale. Use the confirmed data instead of
                // fabricating a duplicate.
                resolvedEvents = sourceEvents.filterNot { it.unitId == cow.id } + dbEventsForCow
                latestDeworming = confirmedLatestDeworming
            }
        }

        val dueDate = calculateNextDewormingDate(cow, latestDeworming)
        val dueDateText = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dueDate)

        val cycleDescription = if (latestDeworming == null) {
            "No deworming log is recorded. Add a completed deworming log after treatment so the next date is calculated automatically."
        } else if (parseFarmDate(cow.dob)?.plusCalendarAmount(Calendar.MONTH, 6)?.let { !dueDate.before(it) } == true) {
            "This cow is on the adult three-month deworming cycle."
        } else {
            "This calf is on the monthly deworming cycle until six months of age."
        }

        val preserveCompletion = when {
            existingTask == null -> false
            existingTask.isCompleted && !isDueTodayOrEarlier(dueDateText) -> true
            existingTask.isCompleted && sameFarmDate(existingTask.scheduledTime, dueDateText) -> true
            else -> false
        }

        val synchronizedTask = FarmTask(
            id = existingTask?.id ?: 0L,
            syncId = taskSyncId,
            farmId = cow.farmId,
            title = "Routine Deworming Treatment",
            category = TaskCategory.LIVESTOCK,
            targetUnit = targetName,
            priority = if (preserveCompletion) TaskPriority.LOW else TaskPriority.HIGH,
            scheduledTime = dueDateText,
            instructions = cycleDescription,
            assignedWorker = "Lead Operator",
            isCompleted = preserveCompletion,
            completedAt = if (preserveCompletion) (existingTask?.completedAt ?: SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())) else null,
            proofPhotoUri = if (preserveCompletion) existingTask?.proofPhotoUri else null,
            proofNotes = if (preserveCompletion) existingTask?.proofNotes else null,
            updatedAt = System.currentTimeMillis()
        )

        candidateTasks
            .filter { it.id != existingTask?.id }
            .forEach { duplicate -> repository.deleteTask(duplicate.id) }

        if (existingTask == null) {
            repository.insertTask(synchronizedTask)
        } else {
            repository.updateTask(synchronizedTask)
        }
    }

    private fun sameFarmDate(left: String, right: String): Boolean {
        val leftDate = parseFarmDate(left)
        val rightDate = parseFarmDate(right)
        return if (leftDate != null && rightDate != null) {
            leftDate.time == rightDate.time
        } else {
            left.trim().equals(right.trim(), ignoreCase = true)
        }
    }

    /** Rebuilds the repeat-heat checks so they always match the current source event. */
    private suspend fun synchronizeRepeatHeatCheckTasks(event: CattleEvent, cow: FarmUnit) {
        val sourcePrefix = "repeat-heat-${event.syncId}-day-"
        repository.softDeleteTasksBySyncIdPrefix(sourcePrefix, event.farmId)
        if (isRecordedHeatEvent(event.category, event.title, event.details)) {
            createRepeatHeatCheckTasks(event, cow)
        }
    }

    /** Creates one visible livestock reminder for each repeat-heat check day (18–21). */
    private suspend fun createRepeatHeatCheckTasks(event: CattleEvent, cow: FarmUnit) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply { isLenient = false }
        val heatDate = runCatching { dateFormat.parse(event.date.trim()) }.getOrNull() ?: return
        val eventKey = event.syncId.ifBlank { "${event.farmId}-${event.unitId}-${event.date}" }
        val targetName = if (cow.tagNumber.isBlank()) cow.name else "${cow.name} • Tag ${cow.tagNumber}"

        for (day in 18..21) {
            val calendar = Calendar.getInstance().apply {
                time = heatDate
                add(Calendar.DAY_OF_YEAR, day)
            }
            val dueDate = dateFormat.format(calendar.time)
            val taskSyncId = "repeat-heat-$eventKey-day-$day"
            if (repository.getTaskBySyncIdForFarm(cow.farmId, taskSyncId) != null) continue

            repository.insertTask(
                FarmTask(
                    syncId = taskSyncId,
                    farmId = event.farmId,
                    title = "Repeat heat check — ${cow.name} (Day $day)",
                    category = TaskCategory.LIVESTOCK,
                    targetUnit = targetName,
                    priority = TaskPriority.HIGH,
                    scheduledTime = dueDate,
                    instructions = "Check for repeat heat on day $day of the 18–21 day window after heat was recorded on ${event.date}. This reminder is created whether or not the cow was inseminated.",
                    assignedWorker = "Lead Operator"
                )
            )
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

    fun saveFeedPlan(plan: FeedPlan) = viewModelScope.launch {
        val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
        repository.saveFeedPlan(plan.copy(farmId = farmId))
    }
    fun deleteFeedPlan(id: Long) = viewModelScope.launch { repository.deleteFeedPlan(id) }
    fun runAutomaticFeedDeductions() = viewModelScope.launch {
        val farmId = currentSession.value?.farmId ?: "FARM-DEFAULT"
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        repository.processAutomaticFeedDeductions(farmId, farmSettings.value, today)
    }
    fun setAutomaticFeedDeductionEnabled(enabled: Boolean) = viewModelScope.launch {
        updateSettings(farmSettings.value.copy(automaticFeedDeductionEnabled = enabled, updatedAt = System.currentTimeMillis()))
    }

}
