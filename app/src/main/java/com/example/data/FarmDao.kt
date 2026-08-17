package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    // Tasks
    @Query("SELECT * FROM farm_tasks WHERE farmId = :farmId OR farmId = 'FARM-DEFAULT' ORDER BY isCompleted ASC, priority ASC, id DESC")
    fun getTasksByFarm(farmId: String): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks ORDER BY isCompleted ASC, priority ASC, id DESC")
    fun getAllTasks(): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isCompleted = 0 ORDER BY priority ASC, id DESC")
    fun getPendingTasks(farmId: String): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE isCompleted = 0 ORDER BY priority ASC, id DESC")
    fun getPendingTasks(): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND isCompleted = 1 ORDER BY id DESC")
    fun getCompletedTasks(farmId: String): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE isCompleted = 1 ORDER BY id DESC")
    fun getCompletedTasks(): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): FarmTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FarmTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<FarmTask>)

    @Update
    suspend fun updateTask(task: FarmTask)

    @Query("DELETE FROM farm_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // Units / Livestock & Crops
    @Query("SELECT * FROM farm_units WHERE farmId = :farmId OR farmId = 'FARM-DEFAULT' ORDER BY name ASC")
    fun getUnitsByFarm(farmId: String): Flow<List<FarmUnit>>

    @Query("SELECT * FROM farm_units ORDER BY name ASC")
    fun getAllUnits(): Flow<List<FarmUnit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: FarmUnit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<FarmUnit>)

    @Update
    suspend fun updateUnit(unit: FarmUnit)

    @Query("DELETE FROM farm_units WHERE id = :id")
    suspend fun deleteUnitById(id: Long)

    @Query("SELECT COUNT(*) FROM farm_tasks")
    suspend fun getTaskCount(): Int

    @Query("SELECT COUNT(*) FROM farm_units")
    suspend fun getUnitCount(): Int

    // Milk Logs
    @Query("SELECT * FROM milk_logs WHERE farmId = :farmId OR farmId = 'FARM-DEFAULT' ORDER BY id DESC")
    fun getMilkLogsByFarm(farmId: String): Flow<List<MilkLog>>

    @Query("SELECT * FROM milk_logs ORDER BY id DESC")
    fun getAllMilkLogs(): Flow<List<MilkLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkLog(log: MilkLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkLogs(logs: List<MilkLog>)

    @Query("DELETE FROM milk_logs WHERE id = :id")
    suspend fun deleteMilkLogById(id: Long)

    @Update
    suspend fun updateMilkLog(log: MilkLog)

    // Egg Logs
    @Query("SELECT * FROM egg_logs WHERE farmId = :farmId OR farmId = 'FARM-DEFAULT' ORDER BY id DESC")
    fun getEggLogsByFarm(farmId: String): Flow<List<EggLog>>

    @Query("SELECT * FROM egg_logs ORDER BY id DESC")
    fun getAllEggLogs(): Flow<List<EggLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEggLog(log: EggLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEggLogs(logs: List<EggLog>)

    @Query("DELETE FROM egg_logs WHERE id = :id")
    suspend fun deleteEggLogById(id: Long)

    @Update
    suspend fun updateEggLog(log: EggLog)

    // Finance Records
    @Query("SELECT * FROM finance_records WHERE farmId = :farmId OR farmId = 'FARM-DEFAULT' ORDER BY id DESC")
    fun getFinanceRecordsByFarm(farmId: String): Flow<List<FinanceRecord>>

    @Query("SELECT * FROM finance_records ORDER BY id DESC")
    fun getAllFinanceRecords(): Flow<List<FinanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceRecord(record: FinanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceRecords(records: List<FinanceRecord>)

    @Query("DELETE FROM finance_records WHERE id = :id")
    suspend fun deleteFinanceRecordById(id: Long)

    // Employee Requests
    @Query("SELECT * FROM employee_requests WHERE farmId = :farmId OR farmId = 'FARM-DEFAULT' ORDER BY id DESC")
    fun getEmployeeRequestsByFarm(farmId: String): Flow<List<EmployeeRequest>>

    @Query("SELECT * FROM employee_requests WHERE (farmId = :farmId OR farmId = 'FARM-DEFAULT') AND (workerId = :workerId OR workerEmailOrPhone = :emailOrPhone OR employeeName = :name) ORDER BY id DESC")
    fun getEmployeeRequestsForWorker(farmId: String, workerId: String, emailOrPhone: String, name: String): Flow<List<EmployeeRequest>>

    @Query("SELECT * FROM employee_requests ORDER BY id DESC")
    fun getAllEmployeeRequests(): Flow<List<EmployeeRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployeeRequest(request: EmployeeRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployeeRequests(requests: List<EmployeeRequest>)

    @Update
    suspend fun updateEmployeeRequest(request: EmployeeRequest)

    @Query("DELETE FROM employee_requests WHERE id = :id")
    suspend fun deleteEmployeeRequestById(id: Long)

    // Settings
    @Query("SELECT * FROM farm_settings WHERE farmId = :farmId LIMIT 1")
    fun getSettingsByFarm(farmId: String): Flow<FarmSettings?>

    @Query("SELECT * FROM farm_settings WHERE id = 1")
    fun getSettings(): Flow<FarmSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: FarmSettings)

    // Worker Accounts
    @Query("SELECT * FROM worker_accounts WHERE farmId = :farmId ORDER BY createdAt DESC")
    fun getWorkersByFarm(farmId: String): Flow<List<WorkerAccount>>

    @Query("SELECT * FROM worker_accounts WHERE workerId = :workerId")
    suspend fun getWorkerById(workerId: String): WorkerAccount?

    @Query("SELECT * FROM worker_accounts WHERE emailOrPhone = :emailOrPhone LIMIT 1")
    suspend fun getWorkerByLoginIdentifier(emailOrPhone: String): WorkerAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerAccount)

    @Update
    suspend fun updateWorker(worker: WorkerAccount)

    @Query("DELETE FROM worker_accounts WHERE workerId = :workerId")
    suspend fun deleteWorkerById(workerId: String)

    @Query("UPDATE worker_accounts SET isRevoked = :isRevoked WHERE workerId = :workerId")
    suspend fun setWorkerRevoked(workerId: String, isRevoked: Boolean)

    // Cattle Events
    @Query("SELECT * FROM cattle_events WHERE unitId = :unitId ORDER BY date DESC")
    fun getCattleEventsByUnit(unitId: Long): Flow<List<CattleEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCattleEvent(event: CattleEvent): Long

    @Update
    suspend fun updateCattleEvent(event: CattleEvent)

    @Query("DELETE FROM cattle_events WHERE id = :id")
    suspend fun deleteCattleEventById(id: Long)

    // Farm Accounts
    @Query("SELECT * FROM farm_accounts WHERE farmId = :farmId LIMIT 1")
    suspend fun getFarmAccount(farmId: String): FarmAccount?

    @Query("SELECT * FROM farm_accounts WHERE ownerEmailOrPhone = :emailOrPhone LIMIT 1")
    suspend fun getFarmAccountByOwner(emailOrPhone: String): FarmAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmAccount(farm: FarmAccount)
}
