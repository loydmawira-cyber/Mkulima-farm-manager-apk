package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

interface SyncableEntity {
    val syncId: String
    val farmId: String
    val updatedAt: Long
    val isDeleted: Boolean
}

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

data class WorkerPermissions(
    val canViewLivestock: Boolean = true,
    val canEditLivestock: Boolean = true,
    val canViewLogs: Boolean = true,
    val canEditLogs: Boolean = true,
    val canViewFinance: Boolean = false,
    val canEditFinance: Boolean = false,
    val canViewTasks: Boolean = true,
    val canCompleteTasks: Boolean = true,
    val canViewRequests: Boolean = true
)

@Entity(tableName = "worker_accounts")
data class WorkerAccount(
    @PrimaryKey val workerId: String,       // e.g. "WRK-89214"
    override val syncId: String = workerId,
    override val farmId: String,                     // Owner's farm ID
    val name: String,                       // Worker's full name
    val emailOrPhone: String,               // Login identifier
    val password: String = "",              // Legacy fallback only (Firebase Auth manages credentials)
    val role: String = "WORKER",
    val isRevoked: Boolean = false,         // Revoked workers cannot log in
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    val canViewLivestock: Boolean = true,
    val canEditLivestock: Boolean = true,
    val canViewLogs: Boolean = true,
    val canEditLogs: Boolean = true,
    val canViewFinance: Boolean = false,
    val canEditFinance: Boolean = false,
    val canViewTasks: Boolean = true,
    val canCompleteTasks: Boolean = true,
    val canViewRequests: Boolean = true
) : SyncableEntity {
    fun toPermissions(): WorkerPermissions {
        return WorkerPermissions(
            canViewLivestock = canViewLivestock,
            canEditLivestock = canEditLivestock,
            canViewLogs = canViewLogs,
            canEditLogs = canEditLogs,
            canViewFinance = canViewFinance,
            canEditFinance = canEditFinance,
            canViewTasks = canViewTasks,
            canCompleteTasks = canCompleteTasks,
            canViewRequests = canViewRequests
        )
    }
}

@Entity(tableName = "farm_accounts")
data class FarmAccount(
    @PrimaryKey val farmId: String,          // e.g. "FARM-82K9"
    val farmName: String,                    // e.g. "Green Pastures Farm"
    val ownerId: String,                     // Owner UID / identifier
    val ownerName: String,                   // Owner Full Name
    val ownerEmailOrPhone: String,           // Owner contact
    val password: String = "",               // Legacy fallback only (Firebase Auth manages credentials)
    val countryCode: String = "+254",        // Country code (e.g. +254)
    val phoneNumber: String = "",            // Phone number
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class UserSession(
    val userId: String,
    val name: String,
    val emailOrPhone: String,
    val role: String,                        // "OWNER" or "WORKER"
    val farmId: String,
    val farmName: String,
    val isRevoked: Boolean = false,
    val permissions: WorkerPermissions = WorkerPermissions(
        canViewLivestock = true,
        canEditLivestock = true,
        canViewLogs = true,
        canEditLogs = true,
        canViewFinance = true,
        canEditFinance = true,
        canViewTasks = true,
        canCompleteTasks = true,
        canViewRequests = true
    )
) {
    val isOwner: Boolean get() = role.equals("OWNER", ignoreCase = true)
}

@Entity(tableName = "farm_tasks")
data class FarmTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val syncId: String = UUID.randomUUID().toString(),
    override val farmId: String = "FARM-DEFAULT",
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
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity

@Entity(tableName = "farm_units")
data class FarmUnit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val syncId: String = UUID.randomUUID().toString(),
    override val farmId: String = "FARM-DEFAULT",
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
    val dam: String = "",
    val photoUri: String? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity

@Entity(tableName = "milk_logs")
data class MilkLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val syncId: String = UUID.randomUUID().toString(),
    override val farmId: String = "FARM-DEFAULT",
    val cowName: String = "Daisy (Friesian)",
    val unitName: String = "Dairy Herd - Friesians",
    val litres: Double,           // Quantity in litres
    val session: String,          // "Morning", "Afternoon", "Evening"
    val fatPercentage: Double = 3.8,
    val date: String = "12 Aug 2026",
    val loggedAt: String = "12 Aug, 06:30 AM",
    val notes: String? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity

@Entity(tableName = "egg_logs")
data class EggLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val syncId: String = UUID.randomUUID().toString(),
    override val farmId: String = "FARM-DEFAULT",
    val unitName: String,         // e.g. "Flock B - Kienyeji Layers"
    val totalEggs: Int,           // e.g. 280
    val damagedEggs: Int = 0,     // e.g. 4
    val grade: String,            // "Grade A", "Grade B", "Mixed"
    val loggedAt: String,         // e.g. "12 Aug, 10:00 AM"
    val notes: String? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity

enum class FinanceType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "finance_records")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val syncId: String = UUID.randomUUID().toString(),
    override val farmId: String = "FARM-DEFAULT",
    val type: FinanceType,        // INCOME or EXPENSE
    val category: String,         // "Milk Sale", "Egg Sale", "Feed Purchase", "Medication", "Salary Advance", "Equipment Maintenance"
    val amount: Double,           // e.g. 8500.00
    val date: String,             // e.g. "12 Aug 2026"
    val description: String,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "farm_settings")
data class FarmSettings(
    @PrimaryKey val id: Int = 1,
    override val syncId: String = "settings",
    override val farmId: String = "FARM-DEFAULT",
    val farmType: String = "Both", // "Cattle Only", "Poultry Only", "Both"
    val currency: String = "KES",  // e.g. "KES", "USD", "EUR"
    val weaningReminderDays: Int = 180, // e.g. 6 months
    val pregnancyCheckReminderDays: Int = 30,
    val dryingOffReminderDays: Int = 60,
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity

@Entity(tableName = "employee_requests")
data class EmployeeRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val syncId: String = UUID.randomUUID().toString(),
    override val farmId: String = "FARM-DEFAULT",
    val workerId: String = "",
    val workerEmailOrPhone: String = "",
    val employeeName: String,     // e.g. "John Kiprono"
    val requestType: String,      // "Salary Advance", "Annual Leave", "Sick Leave", "Emergency Leave"
    val amount: Double = 0.0,     // For advances e.g. 5000.00
    val startDate: String = "",   // For leave e.g. "15 Aug 2026"
    val endDate: String = "",     // For leave e.g. "20 Aug 2026"
    val reason: String,           // e.g. "Medical expenses for child"
    val status: RequestStatus = RequestStatus.PENDING,
    val submittedAt: String,      // e.g. "12 Aug 2026"
    val reviewNotes: String? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity

@Entity(tableName = "cattle_events")
data class CattleEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val syncId: String = UUID.randomUUID().toString(),
    override val farmId: String = "FARM-DEFAULT",
    val unitId: Long = 0,             // Local Foreign key to FarmUnit
    val unitSyncId: String = "",      // Cross-device durable FK to FarmUnit.syncId
    val category: String,         // "PD", "INSEMINATION", "CALVING", etc.
    val title: String,
    val date: String,
    val details: String,
    val notes: String? = null,
    val metricValue: String? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity
