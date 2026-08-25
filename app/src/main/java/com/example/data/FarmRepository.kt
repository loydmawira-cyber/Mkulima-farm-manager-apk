package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FarmRepository(
    val farmDao: FarmDao,
    var syncEngine: FirestoreSyncEngine? = null
) {
    val allTasks: Flow<List<FarmTask>> = farmDao.getAllTasks()
    val pendingTasks: Flow<List<FarmTask>> = farmDao.getPendingTasks()
    val completedTasks: Flow<List<FarmTask>> = farmDao.getCompletedTasks()
    val allUnits: Flow<List<FarmUnit>> = farmDao.getAllUnits()

    val allMilkLogs: Flow<List<MilkLog>> = farmDao.getAllMilkLogs()
    val allEggLogs: Flow<List<EggLog>> = farmDao.getAllEggLogs()
    val allFinanceRecords: Flow<List<FinanceRecord>> = farmDao.getAllFinanceRecords()
    val allEmployeeRequests: Flow<List<EmployeeRequest>> = farmDao.getAllEmployeeRequests()

    val farmSettings: Flow<FarmSettings?> = farmDao.getSettings()

    // Farm-scoped streams
    fun getTasksForFarm(farmId: String): Flow<List<FarmTask>> = farmDao.getTasksByFarm(farmId)
    fun getUnitsForFarm(farmId: String): Flow<List<FarmUnit>> = farmDao.getUnitsByFarm(farmId)
    fun getMilkLogsForFarm(farmId: String): Flow<List<MilkLog>> = farmDao.getMilkLogsByFarm(farmId)
    fun getEggLogsForFarm(farmId: String): Flow<List<EggLog>> = farmDao.getEggLogsByFarm(farmId)
    fun getFinanceRecordsForFarm(farmId: String): Flow<List<FinanceRecord>> = farmDao.getFinanceRecordsByFarm(farmId)
    fun getEmployeeRequestsForFarm(farmId: String): Flow<List<EmployeeRequest>> = farmDao.getEmployeeRequestsByFarm(farmId)
    fun getEmployeeRequestsForWorker(farmId: String, workerId: String, emailOrPhone: String, name: String): Flow<List<EmployeeRequest>> =
        farmDao.getEmployeeRequestsForWorker(farmId, workerId, emailOrPhone, name)
    fun getSettingsForFarm(farmId: String): Flow<FarmSettings?> = farmDao.getSettingsByFarm(farmId)
    fun getWorkersForFarm(farmId: String): Flow<List<WorkerAccount>> = farmDao.getWorkersByFarm(farmId)

    suspend fun getTaskById(id: Long): FarmTask? = farmDao.getTaskById(id)

    suspend fun insertTask(task: FarmTask): Long {
        val prepared = task.copy(
            syncId = if (task.syncId.isBlank()) UUID.randomUUID().toString() else task.syncId,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        val id = farmDao.insertTask(prepared)
        syncEngine?.triggerPush(task.farmId)
        return id
    }

    suspend fun updateTask(task: FarmTask) {
        val prepared = task.copy(
            updatedAt = System.currentTimeMillis()
        )
        farmDao.updateTask(prepared)
        syncEngine?.triggerPush(task.farmId)
    }

    suspend fun deleteTask(id: Long) {
        val task = farmDao.getTaskById(id)
        val now = System.currentTimeMillis()
        farmDao.softDeleteTask(id, now)
        if (task != null) {
            syncEngine?.triggerPush(task.farmId)
        }
    }

    suspend fun insertUnit(unit: FarmUnit): Long {
        val prepared = unit.copy(
            syncId = if (unit.syncId.isBlank()) UUID.randomUUID().toString() else unit.syncId,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        val id = farmDao.insertUnit(prepared)
        syncEngine?.triggerPush(unit.farmId)
        return id
    }

    suspend fun updateUnit(unit: FarmUnit) {
        val prepared = unit.copy(
            updatedAt = System.currentTimeMillis()
        )
        farmDao.updateUnit(prepared)
        syncEngine?.triggerPush(unit.farmId)
    }

    suspend fun deleteUnit(id: Long) {
        val unit = farmDao.getUnitById(id)
        val now = System.currentTimeMillis()
        farmDao.softDeleteUnit(id, now)
        if (unit != null) {
            syncEngine?.triggerPush(unit.farmId)
        }
    }

    suspend fun insertMilkLog(log: MilkLog): Long {
        val prepared = log.copy(
            syncId = if (log.syncId.isBlank()) UUID.randomUUID().toString() else log.syncId,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        val id = farmDao.insertMilkLog(prepared)
        syncEngine?.triggerPush(log.farmId)
        return id
    }

    suspend fun updateMilkLog(log: MilkLog) {
        val prepared = log.copy(
            updatedAt = System.currentTimeMillis()
        )
        farmDao.updateMilkLog(prepared)
        syncEngine?.triggerPush(log.farmId)
    }

    suspend fun deleteMilkLog(id: Long) {
        val now = System.currentTimeMillis()
        val allLogs = farmDao.getDirtyMilkLogs("FARM-DEFAULT", 0)
        val log = allLogs.find { it.id == id }
        farmDao.softDeleteMilkLog(id, now)
        syncEngine?.triggerPush(log?.farmId)
    }

    suspend fun insertEggLog(log: EggLog): Long {
        val prepared = log.copy(
            syncId = if (log.syncId.isBlank()) UUID.randomUUID().toString() else log.syncId,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        val id = farmDao.insertEggLog(prepared)
        syncEngine?.triggerPush(log.farmId)
        return id
    }

    suspend fun updateEggLog(log: EggLog) {
        val prepared = log.copy(
            updatedAt = System.currentTimeMillis()
        )
        farmDao.updateEggLog(prepared)
        syncEngine?.triggerPush(log.farmId)
    }

    suspend fun deleteEggLog(id: Long) {
        val now = System.currentTimeMillis()
        val allLogs = farmDao.getDirtyEggLogs("FARM-DEFAULT", 0)
        val log = allLogs.find { it.id == id }
        farmDao.softDeleteEggLog(id, now)
        syncEngine?.triggerPush(log?.farmId)
    }

    suspend fun insertFinanceRecord(record: FinanceRecord): Long {
        val prepared = record.copy(
            syncId = if (record.syncId.isBlank()) UUID.randomUUID().toString() else record.syncId,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        val id = farmDao.insertFinanceRecord(prepared)
        syncEngine?.triggerPush(record.farmId)
        return id
    }

    suspend fun updateFinanceRecord(record: FinanceRecord) {
        val prepared = record.copy(
            updatedAt = System.currentTimeMillis()
        )
        farmDao.updateFinanceRecord(prepared)
        syncEngine?.triggerPush(record.farmId)
    }

    suspend fun deleteFinanceRecord(id: Long) {
        val now = System.currentTimeMillis()
        val allRecs = farmDao.getDirtyFinanceRecords("FARM-DEFAULT", 0)
        val rec = allRecs.find { it.id == id }
        farmDao.softDeleteFinanceRecord(id, now)
        syncEngine?.triggerPush(rec?.farmId)
    }

    suspend fun insertEmployeeRequest(request: EmployeeRequest): Long {
        val prepared = request.copy(
            syncId = if (request.syncId.isBlank()) UUID.randomUUID().toString() else request.syncId,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        val id = farmDao.insertEmployeeRequest(prepared)
        syncEngine?.triggerPush(request.farmId)
        return id
    }

    suspend fun updateEmployeeRequest(request: EmployeeRequest) {
        val prepared = request.copy(
            updatedAt = System.currentTimeMillis()
        )
        farmDao.updateEmployeeRequest(prepared)
        syncEngine?.triggerPush(request.farmId)
    }

    suspend fun deleteEmployeeRequest(id: Long) {
        val now = System.currentTimeMillis()
        val allReqs = farmDao.getDirtyEmployeeRequests("FARM-DEFAULT", 0)
        val req = allReqs.find { it.id == id }
        farmDao.softDeleteEmployeeRequest(id, now)
        syncEngine?.triggerPush(req?.farmId)
    }

    suspend fun updateSettings(settings: FarmSettings) {
        val prepared = settings.copy(
            syncId = "settings",
            updatedAt = System.currentTimeMillis()
        )
        farmDao.insertSettings(prepared)
        syncEngine?.triggerPush(settings.farmId)
    }

    // Worker operations
    suspend fun insertWorker(worker: WorkerAccount) {
        val prepared = worker.copy(
            syncId = if (worker.syncId.isBlank()) worker.workerId else worker.syncId,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        farmDao.insertWorker(prepared)
        syncEngine?.triggerPush(worker.farmId)
    }

    suspend fun updateWorker(worker: WorkerAccount) {
        val prepared = worker.copy(
            updatedAt = System.currentTimeMillis()
        )
        farmDao.updateWorker(prepared)
        syncEngine?.triggerPush(worker.farmId)
    }

    suspend fun deleteWorker(workerId: String) {
        val worker = farmDao.getWorkerById(workerId)
        val now = System.currentTimeMillis()
        farmDao.softDeleteWorker(workerId, now)
        if (worker != null) {
            syncEngine?.triggerPush(worker.farmId)
        }
    }

    suspend fun setWorkerRevoked(workerId: String, isRevoked: Boolean) {
        val worker = farmDao.getWorkerById(workerId)
        val now = System.currentTimeMillis()
        farmDao.setWorkerRevoked(workerId, isRevoked, now)
        if (worker != null) {
            syncEngine?.triggerPush(worker.farmId)
        }
    }

    suspend fun getWorkerById(workerId: String): WorkerAccount? = farmDao.getWorkerById(workerId)

    suspend fun getWorkerByLoginIdentifier(identifier: String): WorkerAccount? {
        val trimmed = identifier.trim()
        val direct = farmDao.getWorkerByLoginIdentifier(trimmed)
        if (direct != null) return direct

        val cleanDigits = trimmed.replace(Regex("[^0-9]"), "").removePrefix("0")
        if (cleanDigits.length >= 4) {
            val all = farmDao.getAllWorkers()
            for (w in all) {
                val wDigits = w.emailOrPhone.replace(Regex("[^0-9]"), "").removePrefix("0")
                if (wDigits.isNotBlank() && (wDigits.endsWith(cleanDigits) || cleanDigits.endsWith(wDigits))) {
                    return w
                }
                if (w.workerId.equals(trimmed, ignoreCase = true) || w.emailOrPhone.equals(trimmed, ignoreCase = true)) {
                    return w
                }
            }
        }
        return null
    }

    // Cattle Events
    fun getCattleEventsForUnit(unitId: Long): Flow<List<CattleEvent>> = farmDao.getCattleEventsByUnit(unitId)
    fun getAllCattleEvents(farmId: String): Flow<List<CattleEvent>> = farmDao.getAllCattleEvents(farmId)

    suspend fun insertCattleEvent(event: CattleEvent): Long {
        val resolvedUnitSyncId = if (event.unitSyncId.isNotBlank()) {
            event.unitSyncId
        } else if (event.unitId > 0) {
            farmDao.getUnitById(event.unitId)?.syncId ?: ""
        } else ""

        val prepared = event.copy(
            syncId = if (event.syncId.isBlank()) UUID.randomUUID().toString() else event.syncId,
            unitSyncId = resolvedUnitSyncId,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        val id = farmDao.insertCattleEvent(prepared)
        syncEngine?.triggerPush(event.farmId)
        return id
    }

    suspend fun updateCattleEvent(event: CattleEvent) {
        val resolvedUnitSyncId = if (event.unitSyncId.isNotBlank()) {
            event.unitSyncId
        } else if (event.unitId > 0) {
            farmDao.getUnitById(event.unitId)?.syncId ?: ""
        } else ""

        val prepared = event.copy(
            unitSyncId = resolvedUnitSyncId,
            updatedAt = System.currentTimeMillis()
        )
        farmDao.updateCattleEvent(prepared)
        syncEngine?.triggerPush(event.farmId)
    }

    suspend fun deleteCattleEvent(id: Long) {
        val now = System.currentTimeMillis()
        val allEvents = farmDao.getDirtyCattleEvents("FARM-DEFAULT", 0)
        val event = allEvents.find { it.id == id }
        farmDao.softDeleteCattleEvent(id, now)
        syncEngine?.triggerPush(event?.farmId)
    }

    // Reminder Completions (for computed reminders — vaccination, deworming, PD check, etc.
    // that aren't backed by their own FarmTask row)
    fun getReminderCompletionsForFarm(farmId: String): Flow<List<ReminderCompletion>> =
        farmDao.getReminderCompletionsByFarm(farmId)

    suspend fun markReminderComplete(farmId: String, ruleKey: String, unitId: Long) {
        val prepared = ReminderCompletion(
            syncId = UUID.randomUUID().toString(),
            farmId = farmId,
            ruleKey = ruleKey,
            unitId = unitId,
            completedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        farmDao.insertReminderCompletion(prepared)
        syncEngine?.triggerPush(farmId)
    }

    suspend fun clearReminderCompletion(farmId: String, ruleKey: String) {
        farmDao.clearReminderCompletion(farmId, ruleKey)
        syncEngine?.triggerPush(farmId)
    }

    // Farm operations
    suspend fun insertFarmAccount(farm: FarmAccount) = farmDao.insertFarmAccount(farm)
    suspend fun updateFarmAccount(farm: FarmAccount) = farmDao.updateFarmAccount(farm)
    suspend fun getFarmAccount(farmId: String): FarmAccount? = farmDao.getFarmAccount(farmId)
    suspend fun getFarmAccountByOwner(emailOrPhone: String): FarmAccount? {
        val trimmed = emailOrPhone.trim()
        val direct = farmDao.getFarmAccountByOwner(trimmed)
        if (direct != null) return direct

        val cleanDigits = trimmed.replace(Regex("[^0-9]"), "").removePrefix("0")
        if (cleanDigits.length >= 5) {
            val all = farmDao.getAllFarmAccounts()
            for (f in all) {
                val p1 = f.phoneNumber.replace(Regex("[^0-9]"), "").removePrefix("0")
                val p2 = f.ownerEmailOrPhone.replace(Regex("[^0-9]"), "").removePrefix("0")
                if ((p1.isNotBlank() && (p1.endsWith(cleanDigits) || cleanDigits.endsWith(p1))) ||
                    (p2.isNotBlank() && (p2.endsWith(cleanDigits) || cleanDigits.endsWith(p2)))) {
                    return f
                }
                if (f.ownerEmailOrPhone.equals(trimmed, ignoreCase = true) || f.ownerId.equals(trimmed, ignoreCase = true)) {
                    return f
                }
            }
        }
        return null
    }

    suspend fun getAllFarmAccounts(): List<FarmAccount> = farmDao.getAllFarmAccounts()
    suspend fun updateOwnerPassword(emailOrPhone: String, newPass: String) = farmDao.updateOwnerPassword(emailOrPhone, newPass)
    suspend fun updateWorkerPassword(emailOrPhone: String, newPass: String) = farmDao.updateWorkerPassword(emailOrPhone, newPass)

    suspend fun seedNewFarmStarterData(farmId: String, farmName: String) {
        val initialSettings = FarmSettings(
            farmId = farmId,
            syncId = "settings",
            farmType = "Both",
            currency = "KES",
            updatedAt = System.currentTimeMillis()
        )
        farmDao.insertSettings(initialSettings)
        syncEngine?.triggerPush(farmId)
    }

    suspend fun ensureInitialData(database: MkulimaDatabase) {
        // No sample data seeded automatically
    }

    // Only explicit 'wipe everything' actions hard-delete, and those only ever touch local Room, never Firestore.
    suspend fun clearFarmData(farmId: String) {
        farmDao.deleteUnitsForFarm(farmId)
        farmDao.deleteTasksForFarm(farmId)
        farmDao.deleteMilkLogsForFarm(farmId)
        farmDao.deleteEggLogsForFarm(farmId)
        farmDao.deleteFinanceRecordsForFarm(farmId)
        farmDao.deleteEmployeeRequestsForFarm(farmId)
        farmDao.deleteCattleEventsForFarm(farmId)
        farmDao.deleteReminderCompletionsForFarm(farmId)
    }

    suspend fun clearAllData() {
        farmDao.deleteAllUnits()
        farmDao.deleteAllTasks()
        farmDao.deleteAllMilkLogs()
        farmDao.deleteAllEggLogs()
        farmDao.deleteAllFinanceRecords()
        farmDao.deleteAllEmployeeRequests()
        farmDao.deleteAllCattleEvents()
        farmDao.deleteAllReminderCompletions()
    }
}
