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
    suspend fun getWorkerByLoginIdentifier(identifier: String): WorkerAccount? = farmDao.getWorkerByLoginIdentifier(identifier)

    // Cattle Events
    fun getCattleEventsForUnit(unitId: Long): Flow<List<CattleEvent>> = farmDao.getCattleEventsByUnit(unitId)
    suspend fun insertCattleEvent(event: CattleEvent): Long = farmDao.insertCattleEvent(event)
    suspend fun updateCattleEvent(event: CattleEvent) = farmDao.updateCattleEvent(event)
    suspend fun deleteCattleEvent(id: Long) = farmDao.deleteCattleEventById(id)

    // Farm operations
    suspend fun insertFarmAccount(farm: FarmAccount) = farmDao.insertFarmAccount(farm)
    suspend fun updateFarmAccount(farm: FarmAccount) = farmDao.updateFarmAccount(farm)
    suspend fun getFarmAccount(farmId: String): FarmAccount? = farmDao.getFarmAccount(farmId)
    suspend fun getFarmAccountByOwner(emailOrPhone: String): FarmAccount? = farmDao.getFarmAccountByOwner(emailOrPhone)
    suspend fun getAllFarmAccounts(): List<FarmAccount> = farmDao.getAllFarmAccounts()
    suspend fun updateOwnerPassword(emailOrPhone: String, newPass: String) = farmDao.updateOwnerPassword(emailOrPhone, newPass)
    suspend fun updateWorkerPassword(emailOrPhone: String, newPass: String) = farmDao.updateWorkerPassword(emailOrPhone, newPass)

    suspend fun seedNewFarmStarterData(farmId: String, farmName: String) {
        val initialUnits = listOf(
            FarmUnit(
                farmId = farmId,
                name = "Flock B - Kienyeji Layers",
                type = "Poultry",
                headCount = 350,
                healthStatus = "Optimal",
                location = "Poultry Structure 2",
                lastUpdated = "Today, 08:30 AM"
            ),
            FarmUnit(
                farmId = farmId,
                name = "Dairy Herd - Friesians",
                type = "Cattle",
                headCount = 18,
                healthStatus = "Optimal",
                location = "Milking Shed & Paddock",
                lastUpdated = "Today, 05:00 PM"
            )
        )
        farmDao.insertUnits(initialUnits)

        val initialTasks = listOf(
            FarmTask(
                farmId = farmId,
                title = "Morning Herd Inspection & Milking",
                category = TaskCategory.LIVESTOCK,
                targetUnit = "Dairy Herd - Friesians",
                priority = TaskPriority.HIGH,
                scheduledTime = "Today at 06:30 AM",
                isCompleted = false,
                instructions = "Check all cows, clean udder before milking, and record liters in dairy log.",
                assignedWorker = "Lead Operator"
            ),
            FarmTask(
                farmId = farmId,
                title = "Egg Collection & Feed Top-Up",
                category = TaskCategory.LIVESTOCK,
                targetUnit = "Flock B - Kienyeji Layers",
                priority = TaskPriority.MEDIUM,
                scheduledTime = "Today at 10:00 AM",
                isCompleted = false,
                instructions = "Collect eggs, sort into crates, and refill layer mash feeders.",
                assignedWorker = "Poultry Hand"
            )
        )
        farmDao.insertTasks(initialTasks)

        val initialSettings = FarmSettings(
            farmId = farmId,
            farmType = "Both",
            currency = "KES"
        )
        farmDao.insertSettings(initialSettings)
    }

    suspend fun ensureInitialData(database: MkulimaDatabase) {
        if (farmDao.getTaskCount() == 0) {
            val initialUnits = listOf(
                FarmUnit(
                    farmId = "FARM-DEFAULT",
                    name = "Flock B - Kienyeji Layers",
                    type = "Poultry",
                    headCount = 350,
                    healthStatus = "Vaccination Due",
                    location = "Poultry Structure 2",
                    lastUpdated = "Today, 08:30 AM"
                ),
                FarmUnit(
                    farmId = "FARM-DEFAULT",
                    name = "Flock A - Broiler Pen 1",
                    type = "Poultry",
                    headCount = 200,
                    healthStatus = "Optimal",
                    location = "Poultry Structure 1",
                    lastUpdated = "Today, 07:00 AM"
                ),
                FarmUnit(
                    farmId = "FARM-DEFAULT",
                    name = "Dairy Herd - Friesians",
                    type = "Cattle",
                    headCount = 18,
                    healthStatus = "Optimal",
                    location = "Milking Shed & Paddock",
                    lastUpdated = "Yesterday, 05:00 PM"
                ),
                FarmUnit(
                    farmId = "FARM-DEFAULT",
                    name = "Greenhouse 1 - Tomatoes",
                    type = "Greenhouse",
                    headCount = 1200,
                    healthStatus = "Flowering / Drip Check",
                    location = "West Farm Sector",
                    lastUpdated = "Today, 09:15 AM"
                ),
                FarmUnit(
                    farmId = "FARM-DEFAULT",
                    name = "Maize Field Plot A",
                    type = "Open Field",
                    headCount = 2,
                    healthStatus = "Weeding Required",
                    location = "East Valley Plot",
                    lastUpdated = "2 days ago"
                )
            )
            farmDao.insertUnits(initialUnits)

            val initialTasks = listOf(
                FarmTask(
                    farmId = "FARM-DEFAULT",
                    title = "Vaccinate Flock B",
                    category = TaskCategory.LIVESTOCK,
                    targetUnit = "Flock B - Kienyeji Layers",
                    priority = TaskPriority.HIGH,
                    scheduledTime = "Today at 02:30 PM",
                    isCompleted = false,
                    instructions = "Administer Newcastle booster vaccine via drinking water. Ensure all 350 birds are enclosed and water troughs cleaned prior to dosing.",
                    assignedWorker = "Main Farm Hand"
                ),
                FarmTask(
                    farmId = "FARM-DEFAULT",
                    title = "Feed Layer Coop 1",
                    category = TaskCategory.LIVESTOCK,
                    targetUnit = "Flock B - Kienyeji Layers",
                    priority = TaskPriority.MEDIUM,
                    scheduledTime = "Completed Today, 11:30 AM",
                    isCompleted = true,
                    completedAt = "Today at 11:30 AM",
                    proofPhotoUri = "android.resource://com.aistudio.mkulimafarm.xrqz/drawable/farm_vaccination_1786598052984",
                    proofNotes = "Refilled 6 automatic feeders with High-Yield Layer Mash (50kg). Water levels topped up with vit-booster.",
                    assignedWorker = "Mkulima Staff"
                ),
                FarmTask(
                    farmId = "FARM-DEFAULT",
                    title = "Drip Irrigation Inspection",
                    category = TaskCategory.CROPS,
                    targetUnit = "Greenhouse 1 - Tomatoes",
                    priority = TaskPriority.HIGH,
                    scheduledTime = "Completed Today, 09:15 AM",
                    isCompleted = true,
                    completedAt = "Today at 09:15 AM",
                    proofPhotoUri = "android.resource://com.aistudio.mkulimafarm.xrqz/drawable/irrigation_proof_1786598065914",
                    proofNotes = "Checked pump pressure at 2.2 bar. All 12 drip lines flushing cleanly. No clogged emitters observed.",
                    assignedWorker = "Irrigation Tech"
                ),
                FarmTask(
                    farmId = "FARM-DEFAULT",
                    title = "Morning Milk Quality Test & Feed",
                    category = TaskCategory.LIVESTOCK,
                    targetUnit = "Dairy Herd - Friesians",
                    priority = TaskPriority.HIGH,
                    scheduledTime = "Today at 05:00 PM",
                    isCompleted = false,
                    instructions = "Measure yield per cow, log total litres in dairy ledger, and clean stainless steel storage cans with sanitizer.",
                    assignedWorker = "Dairy Lead"
                ),
                FarmTask(
                    farmId = "FARM-DEFAULT",
                    title = "Foliar Fertilizer Spraying",
                    category = TaskCategory.CROPS,
                    targetUnit = "Maize Field Plot A",
                    priority = TaskPriority.MEDIUM,
                    scheduledTime = "Tomorrow at 08:00 AM",
                    isCompleted = false,
                    instructions = "Apply NPK 19:19:19 booster using backpack knapsack sprayer. Wear protective boots and mask.",
                    assignedWorker = "Field Operator"
                )
            )
            farmDao.insertTasks(initialTasks)
        }
    }
}
