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

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminder_completions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `syncId` TEXT NOT NULL,
                `farmId` TEXT NOT NULL,
                `ruleKey` TEXT NOT NULL,
                `unitId` INTEGER NOT NULL,
                `completedAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}


val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `poultry_logs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `syncId` TEXT NOT NULL,
                `farmId` TEXT NOT NULL,
                `unitId` INTEGER NOT NULL,
                `unitSyncId` TEXT NOT NULL,
                `logType` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `feedType` TEXT NOT NULL,
                `quantityKg` REAL NOT NULL,
                `costAmount` REAL NOT NULL,
                `birdCount` INTEGER NOT NULL,
                `cause` TEXT NOT NULL,
                `traysSold` INTEGER NOT NULL,
                `pricePerTray` REAL NOT NULL,
                `totalRevenue` REAL NOT NULL,
                `buyer` TEXT NOT NULL,
                `disposalReason` TEXT NOT NULL,
                `disposalAmount` REAL NOT NULL,
                `vaccineName` TEXT NOT NULL,
                `targetStage` TEXT NOT NULL,
                `vaccineStatus` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `linkedLogSyncId` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}


val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS `inventory_items` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `farmId` TEXT NOT NULL,
            `itemName` TEXT NOT NULL, `category` TEXT NOT NULL, `skuOrBarcode` TEXT NOT NULL, `description` TEXT NOT NULL,
            `quantityAvailable` REAL NOT NULL, `unitOfMeasurement` TEXT NOT NULL, `minimumThreshold` REAL NOT NULL,
            `storageLocation` TEXT NOT NULL, `batchOrLotNumber` TEXT NOT NULL, `purchaseDate` TEXT NOT NULL,
            `expirationDate` TEXT NOT NULL, `unitCost` REAL NOT NULL, `isSilage` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `field_plans` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `farmId` TEXT NOT NULL,
            `fieldName` TEXT NOT NULL, `location` TEXT NOT NULL, `sizeAcres` REAL NOT NULL, `cropName` TEXT NOT NULL,
            `variety` TEXT NOT NULL, `plantedDate` TEXT NOT NULL, `daysToHarvest` INTEGER NOT NULL,
            `estimatedHarvestDate` TEXT NOT NULL, `plantingNotes` TEXT NOT NULL, `status` TEXT NOT NULL,
            `harvestedDate` TEXT NOT NULL, `harvestOutcome` TEXT NOT NULL, `harvestedTonnes` REAL NOT NULL,
            `saleAmount` REAL NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL)""")
    }
}

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
        FarmAccount::class,
        ReminderCompletion::class,
        PoultryLog::class,
        InventoryItem::class,
        FieldPlan::class
    ],
    version = 16,
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
                .addMigrations(MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
                .fallbackToDestructiveMigrationOnDowngrade()
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
            farmDao.insertSettings(FarmSettings(farmId = "FARM-DEFAULT", syncId = "settings"))

            // Default Farm Account without mock operational data
            val defaultFarm = FarmAccount(
                farmId = "FARM-DEFAULT",
                farmName = "My Farm",
                ownerId = "owner_default",
                ownerName = "Farm Owner",
                ownerEmailOrPhone = "owner@mkulima.farm"
            )
            farmDao.insertFarmAccount(defaultFarm)
        }
    }
}
