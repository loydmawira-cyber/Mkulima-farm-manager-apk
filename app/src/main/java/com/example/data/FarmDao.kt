package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farm_tasks ORDER BY isCompleted ASC, priority ASC, id DESC")
    fun getAllTasks(): Flow<List<FarmTask>>

    @Query("SELECT * FROM farm_tasks WHERE isCompleted = 0 ORDER BY priority ASC, id DESC")
    fun getPendingTasks(): Flow<List<FarmTask>>

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
    @Query("SELECT * FROM milk_logs ORDER BY id DESC")
    fun getAllMilkLogs(): Flow<List<MilkLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkLog(log: MilkLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkLogs(logs: List<MilkLog>)

    @Query("DELETE FROM milk_logs WHERE id = :id")
    suspend fun deleteMilkLogById(id: Long)

    // Egg Logs
    @Query("SELECT * FROM egg_logs ORDER BY id DESC")
    fun getAllEggLogs(): Flow<List<EggLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEggLog(log: EggLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEggLogs(logs: List<EggLog>)

    @Query("DELETE FROM egg_logs WHERE id = :id")
    suspend fun deleteEggLogById(id: Long)

    // Finance Records
    @Query("SELECT * FROM finance_records ORDER BY id DESC")
    fun getAllFinanceRecords(): Flow<List<FinanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceRecord(record: FinanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceRecords(records: List<FinanceRecord>)

    @Query("DELETE FROM finance_records WHERE id = :id")
    suspend fun deleteFinanceRecordById(id: Long)

    // Employee Requests
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
}
