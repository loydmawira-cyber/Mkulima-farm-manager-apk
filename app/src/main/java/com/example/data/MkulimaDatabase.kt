package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FarmTask::class,
        FarmUnit::class,
        MilkLog::class,
        EggLog::class,
        FinanceRecord::class,
        EmployeeRequest::class,
        CattleEvent::class,
        FarmSettings::class,
        WorkerAccount::class,
        FarmAccount::class
    ],
    version = 11,
    exportSchema = false
)
abstract class MkulimaDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao

    companion object {
        @Volatile
        private var INSTANCE: MkulimaDatabase? = null

        // Migration from 10 -> 11: add unitId and cowTag to milk_logs, and cowTag to cattle_events
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add nullable columns
                db.execSQL("ALTER TABLE milk_logs ADD COLUMN unitId INTEGER")
                db.execSQL("ALTER TABLE milk_logs ADD COLUMN cowTag TEXT")
                db.execSQL("ALTER TABLE cattle_events ADD COLUMN cowTag TEXT")

                // Best-effort backfill unitId by matching unitName -> farm_units.name
                // This will set unitId only when unitName exactly matches a farm_unit name.
                try {
                    db.execSQL(
                        """
                        UPDATE milk_logs
                        SET unitId = (
                            SELECT id FROM farm_units
                            WHERE farm_units.name = milk_logs.unitName
                            LIMIT 1
                        )
                        WHERE unitId IS NULL
                        """.trimIndent()
                    )
                } catch (e: Exception) {
                    // If backfill fails, leave unitId null; avoid breaking migration
                }
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): MkulimaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MkulimaDatabase::class.java,
                    "mkulima_farm_db"
                )
                    .addMigrations(MIGRATION_10_11)
                    .addCallback(MkulimaDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class MkulimaDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.farmDao())
                }
            }
        }

        suspend fun populateInitialData(farmDao: FarmDao) {
            farmDao.insertSettings(FarmSettings(farmId = "FARM-DEFAULT"))

            // Seed default Farm Account
            val defaultFarm = FarmAccount(
                farmId = "FARM-DEFAULT",
                farmName = "Green Pastures Farm",
                ownerId = "owner_default",
                ownerName = "David Kimani (Farm Owner)",
                ownerEmailOrPhone = "owner@mkulima.farm"
            )
            farmDao.insertFarmAccount(defaultFarm)

            // Seed default Worker Account
            val defaultWorker = WorkerAccount(
                workerId = "WRK-1001",
                farmId = "FARM-DEFAULT",
                name = "John Kiprono (Field Lead)",
                emailOrPhone = "john@mkulima.farm",
                password = "password123",
                role = "WORKER",
                isRevoked = false,
                canViewLivestock = true,
                canEditLivestock = true,
                canViewLogs = true,
                canEditLogs = true,
                canViewFinance = false,
                canEditFinance = false,
                canViewTasks = true,
                canCompleteTasks = true,
                canViewRequests = true
            )
            farmDao.insertWorker(defaultWorker)

            if (farmDao.getTaskCount() == 0) {
                val initialUnits = listOf(
                    FarmUnit(
                        name = "Flock B - Kienyeji Layers",
                        type = "Poultry",
                        headCount = 350,
                        healthStatus = "Vaccination Due",
                        location = "Poultry Structure 2",
                        lastUpdated = "Today, 08:30 AM"
                    ),
                    FarmUnit(
                        name = "Flock A - Broiler Pen 1",
                        type = "Poultry",
                        headCount = 200,
                        healthStatus = "Optimal",
                        location = "Poultry Structure 1",
                        lastUpdated = "Today, 07:00 AM"
                    ),
                    FarmUnit(
                        name = "Dairy Herd - Friesians",
                        type = "Cattle",
                        headCount = 18,
                        healthStatus = "Optimal",
                        location = "Milking Shed & Paddock",
                        lastUpdated = "Yesterday, 05:00 PM"
                    ),
                    FarmUnit(
                        name = "Greenhouse 1 - Tomatoes",
                        type = "Greenhouse",
                        headCount = 1200, // plant count
                        healthStatus = "Flowering / Drip Check",
                        location = "West Farm Sector",
                        lastUpdated = "Today, 09:15 AM"
                    ),
                    FarmUnit(
                        name = "Maize Field Plot A",
                        type = "Open Field",
                        headCount = 2, // acres
                        healthStatus = "Weeding Required",
                        location = "East Valley Plot",
                        lastUpdated = "2 days ago"
                    )
                )
                farmDao.insertUnits(initialUnits)

                val initialTasks = listOf(
                    FarmTask(
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
                        title = "Feed Layer Coop 1",
                        category = TaskCategory.LIVESTOCK,
                        targetUnit = "Flock B - Kienyeji Layers",
                        priority = TaskPriority.MEDIUM,
                        scheduledTime = "Completed Today, 11:30 AM",
                        isCompleted = true,
                        completedAt = "Today at 11:30 AM",
                        proofPhotoUri = "android.resource://com.example/drawable/farm_vaccination_1786598052984",
                        proofNotes = "Refilled 6 automatic feeders with High-Yield Layer Mash (50kg). Water levels topped up with vit-booster.",
                        assignedWorker = "Mkulima Staff"
                    ),
                    FarmTask(
                        title = "Drip Irrigation Inspection",
                        category = TaskCategory.CROPS,
                        targetUnit = "Greenhouse 1 - Tomatoes",
                        priority = TaskPriority.HIGH,
                        scheduledTime = "Completed Today, 09:15 AM",
                        isCompleted = true,
                        completedAt = "Today at 09:15 AM",
                        proofPhotoUri = "android.resource://com.example/drawable/irrigation_proof_1786598065914",
                        proofNotes = "Checked pump pressure at 2.2 bar. All 12 drip lines flushing cleanly. No clogged emitters observed.",
                        assignedWorker = "Irrigation Tech"
                    ),
                    FarmTask(
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

                // Seed Milk Logs (existing seeding left unchanged)
            }
        }
    }
}
