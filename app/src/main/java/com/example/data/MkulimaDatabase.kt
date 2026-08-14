package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
        EmployeeRequest::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MkulimaDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao

    companion object {
        @Volatile
        private var INSTANCE: MkulimaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MkulimaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MkulimaDatabase::class.java,
                    "mkulima_farm_db"
                )
                .fallbackToDestructiveMigration()
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

                // Seed Milk Logs
                val initialMilk = listOf(
                    MilkLog(
                        cowName = "Daisy (Friesian)",
                        unitName = "Dairy Herd - Friesians",
                        litres = 18.5,
                        session = "Morning",
                        fatPercentage = 3.9,
                        date = "12 Aug 2026",
                        loggedAt = "12 Aug, 06:30 AM",
                        notes = "Chilled to 4°C immediately. Delivered to Brookside Dairy Co-op."
                    ),
                    MilkLog(
                        cowName = "Bella (Ayrshire)",
                        unitName = "Dairy Herd - Friesians",
                        litres = 16.0,
                        session = "Morning",
                        fatPercentage = 4.1,
                        date = "12 Aug 2026",
                        loggedAt = "12 Aug, 06:45 AM",
                        notes = "High cream yield."
                    ),
                    MilkLog(
                        cowName = "Bossy (Guernsey)",
                        unitName = "Dairy Herd - Friesians",
                        litres = 14.2,
                        session = "Afternoon",
                        fatPercentage = 3.8,
                        date = "12 Aug 2026",
                        loggedAt = "12 Aug, 01:30 PM",
                        notes = "Mid-day session."
                    ),
                    MilkLog(
                        cowName = "Buttercup (Jersey)",
                        unitName = "Dairy Herd - Friesians",
                        litres = 15.5,
                        session = "Evening",
                        fatPercentage = 4.2,
                        date = "11 Aug 2026",
                        loggedAt = "11 Aug, 05:45 PM",
                        notes = "Evening milking completed without issues."
                    )
                )
                farmDao.insertMilkLogs(initialMilk)

                // Seed Egg Logs
                val initialEggs = listOf(
                    EggLog(
                        unitName = "Flock B - Kienyeji Layers",
                        totalEggs = 310,
                        damagedEggs = 5,
                        grade = "Grade A",
                        loggedAt = "12 Aug, 10:15 AM",
                        notes = "10 crates packed and ready for Nakuru market vendor."
                    ),
                    EggLog(
                        unitName = "Flock B - Kienyeji Layers",
                        totalEggs = 295,
                        damagedEggs = 3,
                        grade = "Grade A",
                        loggedAt = "11 Aug, 04:30 PM",
                        notes = "Refilled calcium supplements in feeder."
                    )
                )
                farmDao.insertEggLogs(initialEggs)

                // Seed Finance Records
                val initialFinances = listOf(
                    FinanceRecord(
                        type = FinanceType.INCOME,
                        category = "Milk Sale",
                        amount = 10110.00,
                        date = "12 Aug 2026",
                        description = "168.5 Litres Morning Milk to Co-op @ 60/L"
                    ),
                    FinanceRecord(
                        type = FinanceType.INCOME,
                        category = "Egg Sale",
                        amount = 4500.00,
                        date = "12 Aug 2026",
                        description = "10 Crates Grade A Kienyeji Eggs @ 450/crate"
                    ),
                    FinanceRecord(
                        type = FinanceType.EXPENSE,
                        category = "Feed Purchase",
                        amount = 6800.00,
                        date = "11 Aug 2026",
                        description = "4 Bags High-Yield Layer Mash (50kg)"
                    ),
                    FinanceRecord(
                        type = FinanceType.EXPENSE,
                        category = "Medication",
                        amount = 2200.00,
                        date = "10 Aug 2026",
                        description = "Newcastle Booster Vaccines & Vitamin Supplements"
                    )
                )
                farmDao.insertFinanceRecords(initialFinances)

                // Seed Employee Requests
                val initialRequests = listOf(
                    EmployeeRequest(
                        employeeName = "John Kiprono",
                        requestType = "Salary Advance",
                        amount = 4000.00,
                        reason = "Emergency medical checkup for child",
                        status = RequestStatus.PENDING,
                        submittedAt = "12 Aug 2026"
                    ),
                    EmployeeRequest(
                        employeeName = "Mary Wambui",
                        requestType = "Annual Leave",
                        startDate = "18 Aug 2026",
                        endDate = "25 Aug 2026",
                        reason = "Family gathering in Eldoret",
                        status = RequestStatus.APPROVED,
                        submittedAt = "10 Aug 2026"
                    )
                )
                farmDao.insertEmployeeRequests(initialRequests)
            }
        }
    }
}
