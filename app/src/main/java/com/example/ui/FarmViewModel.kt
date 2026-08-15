package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EggLog
import com.example.data.EmployeeRequest
import com.example.data.FarmRepository
import com.example.data.FarmSettings
import com.example.data.FarmTask
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.MkulimaDatabase
import com.example.data.RequestStatus
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TaskStatusFilter { ALL, PENDING, COMPLETED, HIGH_PRIORITY }

class FarmViewModel(private val repository: FarmRepository) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<TaskCategory?>(null)
    val selectedStatusFilter = MutableStateFlow(TaskStatusFilter.ALL)

    val allUnits: StateFlow<List<FarmUnit>> = repository.allUnits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allMilkLogs: StateFlow<List<MilkLog>> = repository.allMilkLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allEggLogs: StateFlow<List<EggLog>> = repository.allEggLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFinanceRecords: StateFlow<List<FinanceRecord>> = repository.allFinanceRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allEmployeeRequests: StateFlow<List<EmployeeRequest>> = repository.allEmployeeRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val farmSettings: StateFlow<FarmSettings> = repository.farmSettings
        .map { it ?: FarmSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FarmSettings()
        )

    fun updateSettings(settings: FarmSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    val rawTasks: StateFlow<List<FarmTask>> = repository.allTasks.stateIn(
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

    fun markTaskIncomplete(taskId: Long) {
        viewModelScope.launch {
            val existingTask = repository.getTaskById(taskId) ?: return@launch
            val updatedTask = existingTask.copy(
                isCompleted = false,
                completedAt = null
            )
            repository.updateTask(updatedTask)
        }
    }

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
            val newTask = FarmTask(
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
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val newUnit = FarmUnit(
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

    fun deleteUnit(unitId: Long) {
        viewModelScope.launch {
            repository.deleteUnit(unitId)
        }
    }

    fun updateUnitHeadCount(unitId: Long, newHeadCount: Int) {
        viewModelScope.launch {
            val existing = repository.allUnits.stateIn(viewModelScope).value.find { it.id == unitId }
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

    fun addMilkLog(
        cowName: String,
        unitName: String,
        litres: Double,
        session: String,
        fatPercentage: Double,
        date: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val todayFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val log = MilkLog(
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

    fun deleteEggLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteEggLog(logId)
        }
    }

    fun addEggLog(
        unitName: String,
        totalEggs: Int,
        damagedEggs: Int,
        grade: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val log = EggLog(
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

    fun addFinanceRecord(
        type: FinanceType,
        category: String,
        amount: Double,
        description: String
    ) {
        viewModelScope.launch {
            val todayFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val record = FinanceRecord(
                type = type,
                category = category.ifBlank { "General" },
                amount = amount,
                date = todayFormatted,
                description = description.ifBlank { "Farm transaction" }
            )
            repository.insertFinanceRecord(record)
        }
    }

    fun addEmployeeRequest(
        employeeName: String,
        requestType: String,
        amount: Double,
        startDate: String,
        endDate: String,
        reason: String
    ) {
        viewModelScope.launch {
            val todayFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val req = EmployeeRequest(
                employeeName = employeeName.ifBlank { "Employee" },
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

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
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
}
