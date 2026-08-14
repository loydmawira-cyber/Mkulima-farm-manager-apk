package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class TaskCategory {
    LIVESTOCK,
    CROPS,
    EQUIPMENT,
    GENERAL
}

@Entity(tableName = "farm_tasks")
data class FarmTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,                           // e.g. "Vaccinate Flock B"
    val category: TaskCategory,                  // e.g. LIVESTOCK, CROPS
    val targetUnit: String,                      // e.g. "Flock B - Layers"
    val priority: TaskPriority,                  // HIGH, MEDIUM, LOW
    val scheduledTime: String,                   // e.g. "Today at 02:30 PM"
    val isCompleted: Boolean = false,
    val completedAt: String? = null,             // e.g. "Today at 01:15 PM"
    val proofPhotoUri: String? = null,           // Uri string or drawable resource name
    val proofNotes: String? = null,              // Completion notes
    val assignedWorker: String? = "Lead Farm Operator",
    val instructions: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "farm_units")
data class FarmUnit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,             // e.g. "Flock B - Layers"
    val type: String,             // "Poultry", "Cattle", "Goats", "Greenhouse", "Open Field"
    val headCount: Int,           // e.g. 350
    val healthStatus: String,     // "Excellent", "Monitoring", "Vaccination Due"
    val location: String,         // e.g. "Coop 2 - North Sector"
    val lastUpdated: String,      // e.g. "Today, 10:00 AM"
    val tagNumber: String = "",
    val breed: String = "",
    val dob: String = "",
    val dateAdded: String = "",
    val weightAtBirth: String = "",
    val currentWeight: String = "",
    val sire: String = "",
    val dam: String = ""
)

@Entity(tableName = "milk_logs")
data class MilkLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cowName: String = "Daisy (Friesian)",
    val unitName: String = "Dairy Herd - Friesians",
    val litres: Double,           // Quantity in litres
    val session: String,          // "Morning", "Afternoon", "Evening"
    val fatPercentage: Double = 3.8,
    val date: String = "12 Aug 2026",
    val loggedAt: String = "12 Aug, 06:30 AM",
    val notes: String? = null
)

@Entity(tableName = "egg_logs")
data class EggLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitName: String,         // e.g. "Flock B - Kienyeji Layers"
    val totalEggs: Int,           // e.g. 280
    val damagedEggs: Int = 0,     // e.g. 4
    val grade: String,            // "Grade A", "Grade B", "Mixed"
    val loggedAt: String,         // e.g. "12 Aug, 10:00 AM"
    val notes: String? = null
)

enum class FinanceType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "finance_records")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: FinanceType,        // INCOME or EXPENSE
    val category: String,         // "Milk Sale", "Egg Sale", "Feed Purchase", "Medication", "Salary Advance", "Equipment Maintenance"
    val amount: Double,           // e.g. 8500.00
    val date: String,             // e.g. "12 Aug 2026"
    val description: String
)

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "employee_requests")
data class EmployeeRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeName: String,     // e.g. "John Kiprono"
    val requestType: String,      // "Salary Advance", "Annual Leave", "Sick Leave", "Emergency Leave"
    val amount: Double = 0.0,     // For advances e.g. 5000.00
    val startDate: String = "",   // For leave e.g. "15 Aug 2026"
    val endDate: String = "",     // For leave e.g. "20 Aug 2026"
    val reason: String,           // e.g. "Medical expenses for child"
    val status: RequestStatus = RequestStatus.PENDING,
    val submittedAt: String       // e.g. "12 Aug 2026"
)
