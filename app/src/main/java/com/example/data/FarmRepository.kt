package com.example.data

import kotlinx.coroutines.flow.Flow

class FarmRepository(private val farmDao: FarmDao) {
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
    suspend fun insertTask(task: FarmTask): Long = farmDao.insertTask(task)
    suspend fun updateTask(task: FarmTask) = farmDao.updateTask(task)
    suspend fun deleteTask(id: Long) = farmDao.deleteTaskById(id)

    suspend fun insertUnit(unit: FarmUnit): Long = farmDao.insertUnit(unit)
    suspend fun updateUnit(unit: FarmUnit) = farmDao.updateUnit(unit)
    suspend fun deleteUnit(id: Long) = farmDao.deleteUnitById(id)

    suspend fun insertMilkLog(log: MilkLog): Long = farmDao.insertMilkLog(log)
    suspend fun updateMilkLog(log: MilkLog) = farmDao.updateMilkLog(log)
    suspend fun deleteMilkLog(id: Long) = farmDao.deleteMilkLogById(id)

    suspend fun insertEggLog(log: EggLog): Long = farmDao.insertEggLog(log)
    suspend fun updateEggLog(log: EggLog) = farmDao.updateEggLog(log)
    suspend fun deleteEggLog(id: Long) = farmDao.deleteEggLogById(id)

    suspend fun insertFinanceRecord(record: FinanceRecord): Long = farmDao.insertFinanceRecord(record)
    suspend fun deleteFinanceRecord(id: Long) = farmDao.deleteFinanceRecordById(id)

    // New: update finance record
    suspend fun updateFinanceRecord(record: FinanceRecord) = farmDao.updateFinanceRecord(record)

    suspend fun insertEmployeeRequest(request: EmployeeRequest): Long = farmDao.insertEmployeeRequest(request)
    suspend fun updateEmployeeRequest(request: EmployeeRequest) = farmDao.updateEmployeeRequest(request)
    suspend fun deleteEmployeeRequest(id: Long) = farmDao.deleteEmployeeRequestById(id)

    suspend fun updateSettings(settings: FarmSettings) = farmDao.insertSettings(settings)

    // Worker operations
    suspend fun insertWorker(worker: WorkerAccount) = farmDao.insertWorker(worker)
    suspend fun updateWorker(worker: WorkerAccount) = farmDao.updateWorker(worker)
    suspend fun deleteWorker(workerId: String) = farmDao.deleteWorkerById(workerId)
    suspend fun setWorkerRevoked(workerId: String, isRevoked: Boolean) = farmDao.setWorkerRevoked(workerId, isRevoked)
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
    suspend fun insertCattleEvent(event: CattleEvent): Long = farmDao.insertCattleEvent(event)
    suspend fun updateCattleEvent(event: CattleEvent) = farmDao.updateCattleEvent(event)
    suspend fun deleteCattleEvent(id: Long) = farmDao.deleteCattleEventById(id)

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
                val p1 = f.phoneNumber?.replace(Regex("[^0-9]"), "")?.removePrefix("0") ?: ""
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
            farmType = "Both",
            currency = "KES"
        )
        farmDao.insertSettings(initialSettings)
    }

    suspend fun ensureInitialData(database: MkulimaDatabase) {
        // No sample data seeded automatically
    }

    suspend fun clearFarmData(farmId: String) {
        farmDao.deleteUnitsForFarm(farmId)
        farmDao.deleteTasksForFarm(farmId)
        farmDao.deleteMilkLogsForFarm(farmId)
        farmDao.deleteEggLogsForFarm(farmId)
        farmDao.deleteFinanceRecordsForFarm(farmId)
        farmDao.deleteEmployeeRequestsForFarm(farmId)
        farmDao.deleteCattleEventsForFarm(farmId)
    }

    suspend fun clearAllData() {
        farmDao.deleteAllUnits()
        farmDao.deleteAllTasks()
        farmDao.deleteAllMilkLogs()
        farmDao.deleteAllEggLogs()
        farmDao.deleteAllFinanceRecords()
        farmDao.deleteAllEmployeeRequests()
        farmDao.deleteAllCattleEvents()
    }
}
