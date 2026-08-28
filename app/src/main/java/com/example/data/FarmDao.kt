package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    // ================= Tasks =================
    @Query("SELECT * FROM farm_tasks WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY isCompleted ASC, priority ASC, id DESC")
    fun getTasksByFarm(farmId: String): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE isDeleted = 0 ORDER BY isCompleted ASC, priority ASC, id DESC")
    fun getAllTasks(): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isCompleted = 0 AND isDeleted = 0 ORDER BY priority ASC, id DESC")
    fun getPendingTasks(farmId: String): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE isCompleted = 0 AND isDeleted = 0 ORDER BY priority ASC, id DESC")
    fun getPendingTasks(): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isCompleted = 1 AND isDeleted = 0 ORDER BY id DESC")
    fun getCompletedTasks(farmId: String): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE isCompleted = 1 AND isDeleted = 0 ORDER BY id DESC")
    fun getCompletedTasks(): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE id = :id AND isDeleted = 0")
    suspend fun getTaskById(id: Long): FarmTask?

    @Query("SELECT * FROM farm_tasks WHERE syncId = :syncId AND isDeleted = 0 LIMIT 1")
    suspend fun getTaskBySyncId(syncId: String): FarmTask?

    @Query("UPDATE farm_tasks SET isDeleted = 1, updatedAt = :updatedAt WHERE syncId LIKE :syncIdPrefix || '%'")
    suspend fun softDeleteTasksBySyncIdPrefix(syncIdPrefix: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM farm_tasks WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyTasks(farmId: String, since: Long): List<FarmTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FarmTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<FarmTask>)

    @Update
    suspend fun updateTask(task: FarmTask)

    @Query("UPDATE farm_tasks SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteTask(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM farm_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // ================= Units / Livestock & Crops =================
    @Query("SELECT * FROM farm_units WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY name ASC")
    fun getUnitsByFarm(farmId: String): Flow<List<FarmUnit>>

    @Query("SELECT * FROM farm_units WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllUnits(): Flow<List<FarmUnit>>

    @Query("SELECT * FROM farm_units WHERE id = :id LIMIT 1")
    suspend fun getUnitById(id: Long): FarmUnit?

    @Query("SELECT * FROM farm_units WHERE syncId = :syncId LIMIT 1")
    suspend fun getUnitBySyncId(syncId: String): FarmUnit?

    // Includes soft-deleted cattle so a tag is never reused after a disposal.
    @Query("SELECT tagNumber FROM farm_units WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND (LOWER(type) LIKE '%cattle%' OR LOWER(type) LIKE '%cow%')")
    suspend fun getAssignedCattleTagsForFarm(farmId: String): List<String>

    @Query("SELECT * FROM farm_units WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyUnits(farmId: String, since: Long): List<FarmUnit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: FarmUnit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<FarmUnit>)

    @Update
    suspend fun updateUnit(unit: FarmUnit)

    @Query("UPDATE farm_units SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteUnit(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM farm_units WHERE id = :id")
    suspend fun deleteUnitById(id: Long)

    @Query("SELECT COUNT(*) FROM farm_tasks WHERE isDeleted = 0")
    suspend fun getTaskCount(): Int

    @Query("SELECT COUNT(*) FROM farm_units WHERE isDeleted = 0")
    suspend fun getUnitCount(): Int

    // ================= Milk Logs =================
    @Query("SELECT * FROM milk_logs WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY id DESC")
    fun getMilkLogsByFarm(farmId: String): Flow<List<MilkLog>>

    @Query("SELECT * FROM milk_logs WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllMilkLogs(): Flow<List<MilkLog>>

    @Query("SELECT * FROM milk_logs WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0")
    suspend fun getActiveMilkLogsForFarmOnce(farmId: String): List<MilkLog>

    @Query("SELECT * FROM milk_logs WHERE syncId = :syncId LIMIT 1")
    suspend fun getMilkLogBySyncId(syncId: String): MilkLog?

    @Query("SELECT * FROM milk_logs WHERE id = :id LIMIT 1")
    suspend fun getMilkLogById(id: Long): MilkLog?

    @Query("SELECT * FROM milk_logs WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyMilkLogs(farmId: String, since: Long): List<MilkLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkLog(log: MilkLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkLogs(logs: List<MilkLog>)

    @Query("UPDATE milk_logs SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteMilkLog(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM milk_logs WHERE id = :id")
    suspend fun deleteMilkLogById(id: Long)

    @Update
    suspend fun updateMilkLog(log: MilkLog)

    // ================= Egg Logs =================
    @Query("SELECT * FROM egg_logs WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY id DESC")
    fun getEggLogsByFarm(farmId: String): Flow<List<EggLog>>

    @Query("SELECT * FROM egg_logs WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllEggLogs(): Flow<List<EggLog>>

    @Query("SELECT * FROM egg_logs WHERE syncId = :syncId LIMIT 1")
    suspend fun getEggLogBySyncId(syncId: String): EggLog?

    @Query("SELECT * FROM egg_logs WHERE id = :id LIMIT 1")
    suspend fun getEggLogById(id: Long): EggLog?

    @Query("SELECT * FROM egg_logs WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyEggLogs(farmId: String, since: Long): List<EggLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEggLog(log: EggLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEggLogs(logs: List<EggLog>)

    @Query("UPDATE egg_logs SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteEggLog(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM egg_logs WHERE id = :id")
    suspend fun deleteEggLogById(id: Long)

    @Update
    suspend fun updateEggLog(log: EggLog)

    // ================= Finance Records =================
    @Query("SELECT * FROM finance_records WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY id DESC")
    fun getFinanceRecordsByFarm(farmId: String): Flow<List<FinanceRecord>>

    @Query("SELECT * FROM finance_records WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllFinanceRecords(): Flow<List<FinanceRecord>>

    @Query("SELECT * FROM finance_records WHERE syncId = :syncId LIMIT 1")
    suspend fun getFinanceRecordBySyncId(syncId: String): FinanceRecord?

    @Query("SELECT * FROM finance_records WHERE id = :id LIMIT 1")
    suspend fun getFinanceRecordById(id: Long): FinanceRecord?

    @Query("SELECT * FROM finance_records WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyFinanceRecords(farmId: String, since: Long): List<FinanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceRecord(record: FinanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceRecords(records: List<FinanceRecord>)

    @Update
    suspend fun updateFinanceRecord(record: FinanceRecord)

    @Query("UPDATE finance_records SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteFinanceRecord(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM finance_records WHERE id = :id")
    suspend fun deleteFinanceRecordById(id: Long)
    
// ================= Monthly Reports =================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyReport(report: MonthlyReport): Long

    @Query("SELECT * FROM monthly_reports WHERE syncId = :syncId LIMIT 1")
    suspend fun getMonthlyReportBySyncId(syncId: String): MonthlyReport?

    @Query("SELECT * FROM monthly_reports WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyMonthlyReports(farmId: String, since: Long): List<MonthlyReport>

    @Query("SELECT * FROM monthly_reports WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY reportMonth DESC")
    fun getMonthlyReportsForFarm(farmId: String): Flow<List<MonthlyReport>>

    @Query("SELECT * FROM monthly_reports WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getMonthlyReportsUpdatedSince(farmId: String, since: Long): List<MonthlyReport>

    @Query("UPDATE monthly_reports SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteMonthlyReport(id: Long, updatedAt: Long = System.currentTimeMillis())
    // ================= Employee Requests =================
    @Query("SELECT * FROM employee_requests WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY id DESC")
    fun getEmployeeRequestsByFarm(farmId: String): Flow<List<EmployeeRequest>>

    @Query("SELECT * FROM employee_requests WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 AND (workerId = :workerId OR workerEmailOrPhone = :emailOrPhone OR employeeName = :name) ORDER BY id DESC")
    fun getEmployeeRequestsForWorker(farmId: String, workerId: String, emailOrPhone: String, name: String): Flow<List<EmployeeRequest>>

    @Query("SELECT * FROM employee_requests WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllEmployeeRequests(): Flow<List<EmployeeRequest>>

    @Query("SELECT * FROM employee_requests WHERE syncId = :syncId LIMIT 1")
    suspend fun getEmployeeRequestBySyncId(syncId: String): EmployeeRequest?

    @Query("SELECT * FROM employee_requests WHERE id = :id LIMIT 1")
    suspend fun getEmployeeRequestById(id: Long): EmployeeRequest?

    @Query("SELECT * FROM employee_requests WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyEmployeeRequests(farmId: String, since: Long): List<EmployeeRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployeeRequest(request: EmployeeRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployeeRequests(requests: List<EmployeeRequest>)

    @Update
    suspend fun updateEmployeeRequest(request: EmployeeRequest)

    @Query("UPDATE employee_requests SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteEmployeeRequest(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM employee_requests WHERE id = :id")
    suspend fun deleteEmployeeRequestById(id: Long)

    // ================= Settings =================
    @Query("SELECT * FROM farm_settings WHERE farmId = :farmId LIMIT 1")
    fun getSettingsByFarm(farmId: String): Flow<FarmSettings?>

    @Query("SELECT * FROM farm_settings WHERE id = 1")
    fun getSettings(): Flow<FarmSettings?>

    @Query("SELECT * FROM farm_settings WHERE farmId = :farmId LIMIT 1")
    suspend fun getSettingsSync(farmId: String): FarmSettings?

    @Query("SELECT * FROM farm_settings WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtySettings(farmId: String, since: Long): List<FarmSettings>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: FarmSettings)

    // ================= Worker Accounts =================
    @Query("SELECT * FROM worker_accounts WHERE farmId = :farmId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getWorkersByFarm(farmId: String): Flow<List<WorkerAccount>>

    @Query("SELECT * FROM worker_accounts WHERE workerId = :workerId LIMIT 1")
    suspend fun getWorkerById(workerId: String): WorkerAccount?

    @Query("SELECT * FROM worker_accounts WHERE syncId = :syncId LIMIT 1")
    suspend fun getWorkerBySyncId(syncId: String): WorkerAccount?

    @Query("SELECT * FROM worker_accounts WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyWorkers(farmId: String, since: Long): List<WorkerAccount>

    @Query("SELECT * FROM worker_accounts WHERE isDeleted = 0")
    suspend fun getAllWorkers(): List<WorkerAccount>

    @Query("SELECT * FROM worker_accounts WHERE (emailOrPhone = :emailOrPhone OR workerId = :emailOrPhone) AND isDeleted = 0 LIMIT 1")
    suspend fun getWorkerByLoginIdentifier(emailOrPhone: String): WorkerAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerAccount)

    @Update
    suspend fun updateWorker(worker: WorkerAccount)

    @Query("UPDATE worker_accounts SET isDeleted = 1, updatedAt = :updatedAt WHERE workerId = :workerId")
    suspend fun softDeleteWorker(workerId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM worker_accounts WHERE workerId = :workerId")
    suspend fun deleteWorkerById(workerId: String)

    @Query("UPDATE worker_accounts SET isRevoked = :isRevoked, updatedAt = :updatedAt WHERE workerId = :workerId")
    suspend fun setWorkerRevoked(workerId: String, isRevoked: Boolean, updatedAt: Long = System.currentTimeMillis())

    // ================= Cattle Events =================
    @Query("SELECT * FROM cattle_events WHERE unitId = :unitId AND isDeleted = 0 ORDER BY date DESC")
    fun getCattleEventsByUnit(unitId: Long): Flow<List<CattleEvent>>

    @Query("SELECT * FROM cattle_events WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY date DESC")
    fun getAllCattleEvents(farmId: String): Flow<List<CattleEvent>>

    @Query("SELECT * FROM cattle_events WHERE syncId = :syncId LIMIT 1")
    suspend fun getCattleEventBySyncId(syncId: String): CattleEvent?

    @Query("SELECT * FROM cattle_events WHERE id = :id LIMIT 1")
    suspend fun getCattleEventById(id: Long): CattleEvent?

    @Query("SELECT * FROM cattle_events WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyCattleEvents(farmId: String, since: Long): List<CattleEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCattleEvent(event: CattleEvent): Long

    @Update
    suspend fun updateCattleEvent(event: CattleEvent)

    @Query("UPDATE cattle_events SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteCattleEvent(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM cattle_events WHERE id = :id")
    suspend fun deleteCattleEventById(id: Long)

    @Query("UPDATE cattle_events SET unitId = :unitId WHERE unitSyncId = :unitSyncId")
    suspend fun updateCattleEventsUnitIdByUnitSyncId(unitSyncId: String, unitId: Long)


    // ================= Poultry Logs =================
    @Query("SELECT * FROM poultry_logs WHERE unitId = :unitId AND isDeleted = 0 ORDER BY date DESC, id DESC")
    fun getPoultryLogsByUnit(unitId: Long): Flow<List<PoultryLog>>

    @Query("SELECT * FROM poultry_logs WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY date DESC, id DESC")
    fun getAllPoultryLogs(farmId: String): Flow<List<PoultryLog>>

    @Query("SELECT * FROM poultry_logs WHERE id = :id LIMIT 1")
    suspend fun getPoultryLogById(id: Long): PoultryLog?

    @Query("SELECT * FROM poultry_logs WHERE syncId = :syncId LIMIT 1")
    suspend fun getPoultryLogBySyncId(syncId: String): PoultryLog?

    @Query("SELECT * FROM poultry_logs WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyPoultryLogs(farmId: String, since: Long): List<PoultryLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoultryLog(log: PoultryLog): Long

    @Update
    suspend fun updatePoultryLog(log: PoultryLog)

    @Query("UPDATE poultry_logs SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeletePoultryLog(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM poultry_logs WHERE id = :id")
    suspend fun deletePoultryLogById(id: Long)

    @Query("UPDATE poultry_logs SET unitId = :unitId WHERE unitSyncId = :unitSyncId")
    suspend fun updatePoultryLogsUnitIdByUnitSyncId(unitSyncId: String, unitId: Long)

    // ================= Farm Accounts =================
    @Query("SELECT * FROM farm_accounts WHERE farmId = :farmId LIMIT 1")
    suspend fun getFarmAccount(farmId: String): FarmAccount?

    @Query("SELECT * FROM farm_accounts WHERE ownerEmailOrPhone = :emailOrPhone OR phoneNumber = :emailOrPhone LIMIT 1")
    suspend fun getFarmAccountByOwner(emailOrPhone: String): FarmAccount?

    @Query("SELECT * FROM farm_accounts")
    suspend fun getAllFarmAccounts(): List<FarmAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmAccount(farm: FarmAccount)

    @Update
    suspend fun updateFarmAccount(farm: FarmAccount)

    @Query("UPDATE farm_accounts SET password = :newPassword, updatedAt = :updatedAt WHERE ownerEmailOrPhone = :emailOrPhone OR phoneNumber = :emailOrPhone")
    suspend fun updateOwnerPassword(emailOrPhone: String, newPassword: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE worker_accounts SET password = :newPassword, updatedAt = :updatedAt WHERE emailOrPhone = :emailOrPhone OR workerId = :emailOrPhone")
    suspend fun updateWorkerPassword(emailOrPhone: String, newPassword: String, updatedAt: Long = System.currentTimeMillis())


    // ================= Assets: Inventory =================
    @Query("SELECT * FROM inventory_items WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY itemName COLLATE NOCASE")
    fun getInventoryItemsByFarm(farmId: String): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE syncId = :syncId LIMIT 1")
    suspend fun getInventoryItemBySyncId(syncId: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyInventoryItems(farmId: String, since: Long): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE farmId = :farmId AND category = 'Silage' AND isDeleted = 0 ORDER BY id LIMIT 1")
    suspend fun getSilageItem(farmId: String): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem): Long

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteInventoryItem(id: Long, updatedAt: Long = System.currentTimeMillis())


    // ================= Feed automation & inventory movement ledger =================
    @Query("SELECT * FROM feed_plans WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY targetUnitName, consumptionKind")
    fun getFeedPlansByFarm(farmId: String): Flow<List<FeedPlan>>
    @Query("SELECT * FROM feed_plans WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isEnabled = 1 AND isDeleted = 0")
    suspend fun getEnabledFeedPlans(farmId: String): List<FeedPlan>
    @Query("SELECT * FROM feed_plans WHERE syncId = :syncId LIMIT 1")
    suspend fun getFeedPlanBySyncId(syncId: String): FeedPlan?
    @Query("SELECT * FROM feed_plans WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyFeedPlans(farmId: String, since: Long): List<FeedPlan>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertFeedPlan(plan: FeedPlan): Long
    @Update suspend fun updateFeedPlan(plan: FeedPlan)
    @Query("UPDATE feed_plans SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id") suspend fun softDeleteFeedPlan(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM inventory_movements WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY occurredOn DESC, id DESC")
    fun getInventoryMovementsByFarm(farmId: String): Flow<List<InventoryMovement>>
    @Query("SELECT * FROM inventory_movements WHERE syncId = :syncId LIMIT 1")
    suspend fun getInventoryMovementBySyncId(syncId: String): InventoryMovement?
    @Query("SELECT * FROM inventory_movements WHERE sourceKey = :sourceKey AND isDeleted = 0 LIMIT 1")
    suspend fun getInventoryMovementBySourceKey(sourceKey: String): InventoryMovement?
    @Query("SELECT * FROM inventory_items WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getInventoryItemById(id: Long): InventoryItem?
    @Query("SELECT * FROM inventory_movements WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since") suspend fun getDirtyInventoryMovements(farmId: String, since: Long): List<InventoryMovement>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertInventoryMovement(movement: InventoryMovement): Long

    // ================= Assets: Fields =================
    @Query("SELECT * FROM field_plans WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0 ORDER BY plantedDate DESC")
    fun getFieldPlansByFarm(farmId: String): Flow<List<FieldPlan>>

    @Query("SELECT * FROM field_plans WHERE syncId = :syncId LIMIT 1")
    suspend fun getFieldPlanBySyncId(syncId: String): FieldPlan?

    @Query("SELECT * FROM field_plans WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getFieldPlanById(id: Long): FieldPlan?

    @Query("SELECT * FROM field_plans WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyFieldPlans(farmId: String, since: Long): List<FieldPlan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldPlan(field: FieldPlan): Long

    @Update
    suspend fun updateFieldPlan(field: FieldPlan)

    @Query("UPDATE field_plans SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteFieldPlan(id: Long, updatedAt: Long = System.currentTimeMillis())

    // Clear operational data for a farm (Local Room only)
    @Query("DELETE FROM farm_units WHERE farmId = :farmId")
    suspend fun deleteUnitsForFarm(farmId: String)

    @Query("DELETE FROM farm_tasks WHERE farmId = :farmId")
    suspend fun deleteTasksForFarm(farmId: String)

    @Query("DELETE FROM milk_logs WHERE farmId = :farmId")
    suspend fun deleteMilkLogsForFarm(farmId: String)

    @Query("DELETE FROM egg_logs WHERE farmId = :farmId")
    suspend fun deleteEggLogsForFarm(farmId: String)

    @Query("DELETE FROM finance_records WHERE farmId = :farmId")
    suspend fun deleteFinanceRecordsForFarm(farmId: String)

    @Query("DELETE FROM employee_requests WHERE farmId = :farmId")
    suspend fun deleteEmployeeRequestsForFarm(farmId: String)

    @Query("DELETE FROM cattle_events WHERE farmId = :farmId")
    suspend fun deleteCattleEventsForFarm(farmId: String)

    @Query("DELETE FROM poultry_logs WHERE farmId = :farmId")
    suspend fun deletePoultryLogsForFarm(farmId: String)

    // Clear all operational data across database (Local Room only)
    @Query("DELETE FROM farm_units")
    suspend fun deleteAllUnits()

    @Query("DELETE FROM farm_tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM milk_logs")
    suspend fun deleteAllMilkLogs()

    @Query("DELETE FROM egg_logs")
    suspend fun deleteAllEggLogs()

    @Query("DELETE FROM finance_records")
    suspend fun deleteAllFinanceRecords()

    @Query("DELETE FROM employee_requests")
    suspend fun deleteAllEmployeeRequests()

    @Query("DELETE FROM cattle_events")
    suspend fun deleteAllCattleEvents()

    @Query("DELETE FROM poultry_logs")
    suspend fun deleteAllPoultryLogs()

    // ================= Reminder Completions =================
    @Query("SELECT * FROM reminder_completions WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isDeleted = 0")
    fun getReminderCompletionsByFarm(farmId: String): Flow<List<ReminderCompletion>>

    @Query("SELECT * FROM reminder_completions WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND ruleKey = :ruleKey AND isDeleted = 0 LIMIT 1")
    suspend fun getReminderCompletion(farmId: String, ruleKey: String): ReminderCompletion?

    @Query("SELECT * FROM reminder_completions WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND updatedAt > :since")
    suspend fun getDirtyReminderCompletions(farmId: String, since: Long): List<ReminderCompletion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderCompletion(completion: ReminderCompletion): Long

    @Query("UPDATE reminder_completions SET isDeleted = 1, updatedAt = :updatedAt WHERE ruleKey = :ruleKey AND (farmId = :farmId OR farmId = 'FARM-DEFAULT')")
    suspend fun clearReminderCompletion(farmId: String, ruleKey: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM reminder_completions WHERE farmId = :farmId")
    suspend fun deleteReminderCompletionsForFarm(farmId: String)

    @Query("DELETE FROM reminder_completions")
    suspend fun deleteAllReminderCompletions()
}
