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
        db.execSQL("""
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
        """.trimIndent())
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
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
        """.trimIndent())
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

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `farm_settings` ADD COLUMN `automaticFeedDeductionEnabled` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `farm_settings` ADD COLUMN `feedDeductionLastRunDate` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `intendedLivestockType` TEXT NOT NULL DEFAULT 'GENERAL'")
        db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `intendedUnitId` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE TABLE IF NOT EXISTS `feed_plans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `farmId` TEXT NOT NULL, `targetUnitId` INTEGER NOT NULL, `targetUnitSyncId` TEXT NOT NULL, `targetUnitName` TEXT NOT NULL, `livestockType` TEXT NOT NULL, `inventoryItemId` INTEGER NOT NULL, `inventoryItemSyncId` TEXT NOT NULL, `inventoryItemName` TEXT NOT NULL, `consumptionKind` TEXT NOT NULL, `dailyQuantityKg` REAL NOT NULL, `isEnabled` INTEGER NOT NULL, `lastProcessedDate` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `inventory_movements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `farmId` TEXT NOT NULL, `inventoryItemId` INTEGER NOT NULL, `inventoryItemName` TEXT NOT NULL, `targetUnitId` INTEGER NOT NULL, `targetUnitName` TEXT NOT NULL, `movementType` TEXT NOT NULL, `quantityDeltaKg` REAL NOT NULL, `balanceAfterKg` REAL NOT NULL, `occurredOn` TEXT NOT NULL, `sourceKey` TEXT NOT NULL, `notes` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL)")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `farm_settings` ADD COLUMN `monthlyReportsEnabled` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `monthly_reports` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `syncId` TEXT NOT NULL,
            `farmId` TEXT NOT NULL,
            `reportMonth` TEXT NOT NULL,
            `title` TEXT NOT NULL,
            `generatedAt` INTEGER NOT NULL,
            `fileUrl` TEXT NOT NULL,
            `storageKey` TEXT NOT NULL,
            `totalIncome` REAL NOT NULL,
            `totalExpense` REAL NOT NULL,
            `netBalance` REAL NOT NULL,
            `inventoryItemCount` INTEGER NOT NULL,
            `inventoryValue` REAL NOT NULL,
            `totalMilkLitres` REAL NOT NULL,
            `totalEggs` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL,
            `isDeleted` INTEGER NOT NULL
        )""")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `farm_units` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [
        FarmTask::class,
        FarmUnit::class,
        MilkLog::class,
        EggLog::class,
        FinanceRecord::class,
        MonthlyReport::class,
        EmployeeRequest::class,
        CattleEvent::class,
        FarmSettings::class,
        WorkerAccount::class,
        FarmAccount::class,
        ReminderCompletion::class,
        PoultryLog::class,
        InventoryItem::class,
        FieldPlan::class,
        FeedPlan::class,
        InventoryMovement::class
    ],
    version = 19,
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
                    .addMigrations(MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(MkulimaDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class MkulimaDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
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
            farmDao.insertFarmAccount(
                FarmAccount(
                    farmId = "FARM-DEFAULT",
                    farmName = "My Farm",
                    ownerId = "owner_default",
                    ownerName = "Farm Owner",
                    ownerEmailOrPhone = "owner@mkulima.farm"
                )
            )
        }
    }
}
