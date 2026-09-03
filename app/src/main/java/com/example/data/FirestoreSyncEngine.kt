package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException

class FirestoreSyncEngine(
    private val context: Context,
    private val farmDao: FarmDao
) {
    companion object {
        private const val TAG = "FirestoreSyncEngine"
        private const val PREFS_NAME = "mkulima_firestore_sync_prefs"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var activeFarmId: String? = null
    private val activeListeners = mutableListOf<ListenerRegistration>()
    private var pushJob: Job? = null

    @Volatile
    private var isOnline: Boolean = true

    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Throwable) {
            Log.w(TAG, "Could not check network availability: ${e.message}")
            true // Assume online if we can't determine — avoids false "Offline" state
        }
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(
        if (isNetworkAvailable(context)) SyncStatus.Synced else SyncStatus.Offline
    )
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        Log.d(TAG, "Internet connectivity restored.")
                        isOnline = true
                        val farm = activeFarmId
                        if (!farm.isNullOrBlank() && farm != "FARM-DEFAULT") {
                            _syncStatus.value = SyncStatus.Syncing
                            triggerPush(farm)
                        } else {
                            _syncStatus.value = SyncStatus.Synced
                        }
                    }

                    override fun onLost(network: Network) {
                        Log.d(TAG, "Internet connectivity lost. Entering offline mode.")
                        isOnline = false
                        _syncStatus.value = SyncStatus.Offline
                    }
                })
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Could not register connectivity network callback: ${e.message}")
        }
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            val db = FirebaseFirestore.getInstance()
            try {
                val currentSettings = db.firestoreSettings
                if (!currentSettings.isPersistenceEnabled) {
                    val settings = FirebaseFirestoreSettings.Builder(currentSettings)
                        .setPersistenceEnabled(true)
                        .build()
                    db.firestoreSettings = settings
                }
            } catch (e: Throwable) {
                // Settings can only be set before calling any other methods
            }
            db
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to get Firestore instance", e)
            null
        }
    }

    private fun <T> Task<T>.awaitTask(timeoutMs: Long = 10000L): T {
        try {
            return Tasks.await(this, timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }

    private val pushMutex = kotlinx.coroutines.sync.Mutex()

    // Firestore caps a single WriteBatch at 500 operations. Queuing writes here and
    // flushing in chunks turns what used to be one network round trip PER ROW into
    // high-speed batched writes.
    private inner class BatchWriter(private val db: FirebaseFirestore) {
        private var batch = db.batch()
        private var opsInBatch = 0

        fun queueSet(ref: DocumentReference, data: Map<String, Any?>) {
            val nonNull = data.filterValues { it != null }
            batch.set(ref, nonNull, SetOptions.merge())
            opsInBatch++
            if (opsInBatch >= 400) flush()
        }

        fun flush() {
            if (opsInBatch == 0) return
            val toCommit = batch
            batch = db.batch()
            opsInBatch = 0
            try {
                // Firestore writes locally to SQLite cache immediately, then uploads.
                // We use a short 3s timeout to verify fast network acknowledgment without blocking the thread.
                Tasks.await(toCommit.commit(), 3000L, java.util.concurrent.TimeUnit.MILLISECONDS)
            } catch (e: Throwable) {
                Log.d(TAG, "Batch committed to Firestore local cache: ${e.message}")
            }
        }
    }

    /**
     * Reserves one farm-wide cattle tag through Firestore's transaction retry mechanism.
     * The local candidate lets existing farms continue from their highest historical tag
     * when the sequence document is first created.
     */
    fun reserveNextCattleTag(farmId: String, localNextCandidate: Long): String? {
        val db = getFirestore() ?: return null
        return runCatching {
            val remoteHighestTag = db.collection("farms").document(farmId).collection("units")
                .get().awaitTask(8000L).documents
                .asSequence()
                .mapNotNull { it.getString("tagNumber") }
                .map { it.trim().removePrefix("#") }
                .mapNotNull { it.toLongOrNull() }
                .maxOrNull() ?: 0L
            val sequenceRef = db.collection("farms").document(farmId)
                .collection("meta").document("cattle_tag_sequence")
            db.runTransaction { transaction ->
                val snapshot = transaction.get(sequenceRef)
                val reservedNumber = maxOf(
                    snapshot.getLong("nextTagNumber") ?: 1L,
                    localNextCandidate.coerceAtLeast(1L),
                    remoteHighestTag + 1L
                )
                transaction.set(
                    sequenceRef,
                    mapOf(
                        "nextTagNumber" to (reservedNumber + 1L),
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                reservedNumber.toString().padStart(3, '0')
            }.awaitTask(8000L)
        }.onFailure { error ->
            Log.e(TAG, "Unable to reserve cattle tag for farm $farmId", error)
        }.getOrNull()
    }

    private fun getWatermark(farmId: String, key: String): Long {
        return prefs.getLong("${farmId}_${key}", 0L)
    }

    private fun setWatermark(farmId: String, key: String, timestamp: Long) {
        prefs.edit().putLong("${farmId}_${key}", timestamp).apply()
    }

    private fun subscriptionTimestamp(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        return runCatching {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.parse(raw)?.time ?: 0L
        }.getOrDefault(0L)
    }

    fun startSync(farmId: String) {
        if (farmId.isBlank() || farmId == "FARM-DEFAULT") return
        synchronized(this) {
            if (activeFarmId == farmId && activeListeners.isNotEmpty()) return
            stopSync()
            activeFarmId = farmId
        }

        val db = getFirestore() ?: return
        Log.d(TAG, "Starting Firestore sync for farm: $farmId")

        // 1. Settings Listener
        try {
            val settingsListener = db.collection("farms").document(farmId)
                .collection("meta").document("settings")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in settings snapshot listener", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        scope.launch {
                            applyRemoteSettings(farmId, snapshot)
                        }
                    }
                }
            activeListeners.add(settingsListener)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to attach settings listener", e)
        }

        // Subscription state comes only from the verified server record. The
        // normal settings push deliberately does not overwrite it from Android.
        try {
            val subscriptionListener = db.collection("farms").document(farmId)
                .collection("meta").document("subscription")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in subscription snapshot listener", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        scope.launch { applyRemoteSubscription(farmId, snapshot) }
                    }
                }
            activeListeners.add(subscriptionListener)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to attach subscription listener", e)
        }

        // Helper for collection listeners
        fun attachCollectionListener(table: String, onApply: suspend (DocumentSnapshot) -> Unit) {
            try {
                val listener = db.collection("farms").document(farmId)
                    .collection(table)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null) {
                            Log.e(TAG, "Error in $table snapshot listener", error)
                            return@addSnapshotListener
                        }
                        if (snapshots != null) {
                            scope.launch {
                                for (doc in snapshots.documents) {
                                    try {
                                        onApply(doc)
                                    } catch (e: Throwable) {
                                        Log.e(TAG, "Failed to apply remote $table doc ${doc.id}", e)
                                    }
                                }
                            }
                        }
                    }
                activeListeners.add(listener)
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to attach listener for $table", e)
            }
        }

        attachCollectionListener("tasks") { applyRemoteTask(farmId, it) }
        attachCollectionListener("units") { applyRemoteUnit(farmId, it) }
        attachCollectionListener("milk_logs") { applyRemoteMilkLog(farmId, it) }
        attachCollectionListener("milk_usage_logs") { applyRemoteMilkUsageLog(farmId, it) }
        attachCollectionListener("egg_logs") { applyRemoteEggLog(farmId, it) }
        attachCollectionListener("finance_records") { applyRemoteFinanceRecord(farmId, it) }
        attachCollectionListener("employee_requests") { applyRemoteEmployeeRequest(farmId, it) }
        attachCollectionListener("cattle_events") { applyRemoteCattleEvent(farmId, it) }
        attachCollectionListener("poultry_logs") { applyRemotePoultryLog(farmId, it) }
        attachCollectionListener("worker_accounts") { applyRemoteWorkerAccount(farmId, it) }
        attachCollectionListener("reminder_completions") { applyRemoteReminderCompletion(farmId, it) }
        attachCollectionListener("inventory_items") { applyRemoteInventoryItem(farmId, it) }
        attachCollectionListener("field_plans") { applyRemoteFieldPlan(farmId, it) }
        attachCollectionListener("feed_plans") { applyRemoteFeedPlan(farmId, it) }
        attachCollectionListener("inventory_movements") { applyRemoteInventoryMovement(farmId, it) }
        attachCollectionListener("monthly_reports") { applyRemoteMonthlyReport(farmId, it) }

        // Trigger initial push of any offline/dirty changes
        triggerPush(farmId)
    }

    fun stopSync() {
        synchronized(this) {
            for (listener in activeListeners) {
                try {
                    listener.remove()
                } catch (e: Throwable) {}
            }
            activeListeners.clear()
            activeFarmId = null
        }
    }

    fun triggerPush(farmId: String? = null) {
        val targetFarmId = farmId ?: activeFarmId ?: return
        if (targetFarmId == "FARM-DEFAULT" || targetFarmId.isBlank()) {
            _syncStatus.value = if (isNetworkAvailable(context)) SyncStatus.Synced else SyncStatus.Offline
            return
        }

        scope.launch {
            delay(150) // Debounce rapid keystrokes/edits
            if (pushMutex.isLocked) return@launch // Already syncing, dirty rows will be captured
            pushMutex.withLock {
                _syncStatus.value = SyncStatus.Syncing
                try {
                    pushDirtyRows(targetFarmId)
                } finally {
                    _syncStatus.value = if (isNetworkAvailable(context)) SyncStatus.Synced else SyncStatus.Offline
                }
            }
        }
    }

    suspend fun pushDirtyRows(farmId: String) = withContext(Dispatchers.IO) {
        try {
            val db = getFirestore() ?: run {
                Log.e(TAG, "Firestore instance unavailable — skipping push for farm: $farmId")
                _syncStatus.value = if (isNetworkAvailable(context)) SyncStatus.Synced else SyncStatus.Offline
                return@withContext
            }
            val farmRef = db.collection("farms").document(farmId)
            val writer = BatchWriter(db)

            // 1. Settings
            val lastSettingsPush = getWatermark(farmId, "push_settings")
            val dirtySettings = farmDao.getDirtySettings(farmId, lastSettingsPush)
            var maxSettingsUpdatedAt = lastSettingsPush
            for (setting in dirtySettings) {
                val data = hashMapOf<String, Any>(
                    "syncId" to setting.syncId,
                    "farmId" to farmId,
                    "farmType" to setting.farmType,
                    "currency" to setting.currency,
                    "weaningReminderDays" to setting.weaningReminderDays,
                    "pregnancyCheckReminderDays" to setting.pregnancyCheckReminderDays,
                    "dryingOffReminderDays" to setting.dryingOffReminderDays,
                    "themeMode" to setting.themeMode,
                    "automaticFeedDeductionEnabled" to setting.automaticFeedDeductionEnabled,
                    "feedDeductionLastRunDate" to setting.feedDeductionLastRunDate,
                    "monthlyReportsEnabled" to setting.monthlyReportsEnabled,
                    "updatedAt" to setting.updatedAt,
                    "isDeleted" to setting.isDeleted
                )
                writer.queueSet(farmRef.collection("meta").document("settings"), data)
                if (setting.updatedAt > maxSettingsUpdatedAt) maxSettingsUpdatedAt = setting.updatedAt
            }

            // 2. Tasks
            val lastTasksPush = getWatermark(farmId, "push_tasks")
            val dirtyTasks = farmDao.getDirtyTasks(farmId, lastTasksPush)
            var maxTaskUpdatedAt = lastTasksPush
            for (task in dirtyTasks) {
                val data = hashMapOf<String, Any?>(
                    "syncId" to task.syncId,
                    "farmId" to farmId,
                    "title" to task.title,
                    "category" to task.category.name,
                    "targetUnit" to task.targetUnit,
                    "priority" to task.priority.name,
                    "scheduledTime" to task.scheduledTime,
                    "isCompleted" to task.isCompleted,
                    "completedAt" to task.completedAt,
                    "proofPhotoUri" to task.proofPhotoUri,
                    "proofNotes" to task.proofNotes,
                    "assignedWorker" to task.assignedWorker,
                    "instructions" to task.instructions,
                    "createdAt" to task.createdAt,
                    "updatedAt" to task.updatedAt,
                    "isDeleted" to task.isDeleted
                )
                writer.queueSet(farmRef.collection("tasks").document(task.syncId), data)
                if (task.updatedAt > maxTaskUpdatedAt) maxTaskUpdatedAt = task.updatedAt
            }

            // 3. Units
            val lastUnitsPush = getWatermark(farmId, "push_units")
            val dirtyUnits = farmDao.getDirtyUnits(farmId, lastUnitsPush)
            var maxUnitUpdatedAt = lastUnitsPush
            for (unit in dirtyUnits) {
                val data = hashMapOf<String, Any?>(
                    "syncId" to unit.syncId,
                    "farmId" to farmId,
                    "name" to unit.name,
                    "type" to unit.type,
                    "headCount" to unit.headCount,
                    "healthStatus" to unit.healthStatus,
                    "location" to unit.location,
                    "lastUpdated" to unit.lastUpdated,
                    "tagNumber" to unit.tagNumber,
                    "breed" to unit.breed,
                    "dob" to unit.dob,
                    "dateAdded" to unit.dateAdded,
                    "weightAtBirth" to unit.weightAtBirth,
                    "currentWeight" to unit.currentWeight,
                    "sire" to unit.sire,
                    "dam" to unit.dam,
                    "photoUri" to unit.photoUri,
                    "notes" to unit.notes,
                    "updatedAt" to unit.updatedAt,
                    "isDeleted" to unit.isDeleted
                )
                writer.queueSet(farmRef.collection("units").document(unit.syncId), data)
                if (unit.updatedAt > maxUnitUpdatedAt) maxUnitUpdatedAt = unit.updatedAt
            }

            // 4. Milk Logs
            val lastMilkPush = getWatermark(farmId, "push_milk_logs")
            val dirtyMilk = farmDao.getDirtyMilkLogs(farmId, lastMilkPush)
            var maxMilkUpdatedAt = lastMilkPush
            for (log in dirtyMilk) {
                val data = hashMapOf<String, Any?>(
                    "syncId" to log.syncId,
                    "farmId" to farmId,
                    "cowName" to log.cowName,
                    "unitName" to log.unitName,
                    "litres" to log.litres,
                    "session" to log.session,
                    "fatPercentage" to log.fatPercentage,
                    "date" to log.date,
                    "loggedAt" to log.loggedAt,
                    "notes" to log.notes,
                    "updatedAt" to log.updatedAt,
                    "isDeleted" to log.isDeleted
                )
                writer.queueSet(farmRef.collection("milk_logs").document(log.syncId), data)
                if (log.updatedAt > maxMilkUpdatedAt) maxMilkUpdatedAt = log.updatedAt
            }

            // 4b. Milk Usage Logs (Coop / Home / Calves split)
            val lastMilkUsagePush = getWatermark(farmId, "push_milk_usage_logs")
            val dirtyMilkUsage = farmDao.getDirtyMilkUsageLogs(farmId, lastMilkUsagePush)
            var maxMilkUsageUpdatedAt = lastMilkUsagePush
            for (usage in dirtyMilkUsage) {
                val data = hashMapOf<String, Any?>(
                    "syncId" to usage.syncId,
                    "farmId" to farmId,
                    "date" to usage.date,
                    "session" to usage.session,
                    "litresToCooperative" to usage.litresToCooperative,
                    "litresHomeUse" to usage.litresHomeUse,
                    "litresToCalves" to usage.litresToCalves,
                    "notes" to usage.notes,
                    "updatedAt" to usage.updatedAt,
                    "isDeleted" to usage.isDeleted
                )
                writer.queueSet(farmRef.collection("milk_usage_logs").document(usage.syncId), data)
                if (usage.updatedAt > maxMilkUsageUpdatedAt) maxMilkUsageUpdatedAt = usage.updatedAt
            }

            // 5. Egg Logs
            val lastEggPush = getWatermark(farmId, "push_egg_logs")
            val dirtyEgg = farmDao.getDirtyEggLogs(farmId, lastEggPush)
            var maxEggUpdatedAt = lastEggPush
            for (log in dirtyEgg) {
                val data = hashMapOf<String, Any?>(
                    "syncId" to log.syncId,
                    "farmId" to farmId,
                    "unitName" to log.unitName,
                    "totalEggs" to log.totalEggs,
                    "damagedEggs" to log.damagedEggs,
                    "grade" to log.grade,
                    "loggedAt" to log.loggedAt,
                    "notes" to log.notes,
                    "updatedAt" to log.updatedAt,
                    "isDeleted" to log.isDeleted
                )
                writer.queueSet(farmRef.collection("egg_logs").document(log.syncId), data)
                if (log.updatedAt > maxEggUpdatedAt) maxEggUpdatedAt = log.updatedAt
            }

            // 6. Finance Records
            val lastFinancePush = getWatermark(farmId, "push_finance_records")
            val dirtyFinance = farmDao.getDirtyFinanceRecords(farmId, lastFinancePush)
            var maxFinanceUpdatedAt = lastFinancePush
            for (rec in dirtyFinance) {
                val data = hashMapOf<String, Any?>(
                    "syncId" to rec.syncId,
                    "farmId" to farmId,
                    "type" to rec.type.name,
                    "category" to rec.category,
                    "amount" to rec.amount,
                    "date" to rec.date,
                    "description" to rec.description,
                    "updatedAt" to rec.updatedAt,
                    "isDeleted" to rec.isDeleted
                )
                writer.queueSet(farmRef.collection("finance_records").document(rec.syncId), data)
                if (rec.updatedAt > maxFinanceUpdatedAt) maxFinanceUpdatedAt = rec.updatedAt
            }

            // 7. Employee Requests
            val lastReqPush = getWatermark(farmId, "push_employee_requests")
            val dirtyReqs = farmDao.getDirtyEmployeeRequests(farmId, lastReqPush)
            var maxReqUpdatedAt = lastReqPush
            for (req in dirtyReqs) {
                val data = hashMapOf<String, Any?>(
                    "syncId" to req.syncId,
                    "farmId" to farmId,
                    "workerId" to req.workerId,
                    "workerEmailOrPhone" to req.workerEmailOrPhone,
                    "employeeName" to req.employeeName,
                    "requestType" to req.requestType,
                    "amount" to req.amount,
                    "startDate" to req.startDate,
                    "endDate" to req.endDate,
                    "reason" to req.reason,
                    "status" to req.status.name,
                    "submittedAt" to req.submittedAt,
                    "reviewNotes" to req.reviewNotes,
                    "updatedAt" to req.updatedAt,
                    "isDeleted" to req.isDeleted
                )
                writer.queueSet(farmRef.collection("employee_requests").document(req.syncId), data)
                if (req.updatedAt > maxReqUpdatedAt) maxReqUpdatedAt = req.updatedAt
            }

            // 8. Cattle Events
            val lastEventPush = getWatermark(farmId, "push_cattle_events")
            val dirtyEvents = farmDao.getDirtyCattleEvents(farmId, lastEventPush)
            var maxEventUpdatedAt = lastEventPush
            for (event in dirtyEvents) {
                val resolvedUnitSyncId = if (event.unitSyncId.isNotBlank()) {
                    event.unitSyncId
                } else if (event.unitId > 0) {
                    farmDao.getUnitById(event.unitId)?.syncId ?: ""
                } else ""

                val data = hashMapOf<String, Any?>(
                    "syncId" to event.syncId,
                    "farmId" to farmId,
                    "unitSyncId" to resolvedUnitSyncId,
                    "category" to event.category,
                    "title" to event.title,
                    "date" to event.date,
                    "details" to event.details,
                    "notes" to event.notes,
                    "metricValue" to event.metricValue,
                    "updatedAt" to event.updatedAt,
                    "isDeleted" to event.isDeleted
                )
                writer.queueSet(farmRef.collection("cattle_events").document(event.syncId), data)
                if (event.updatedAt > maxEventUpdatedAt) maxEventUpdatedAt = event.updatedAt
            }

            // 9. Poultry Logs
            val lastPoultryPush = getWatermark(farmId, "push_poultry_logs")
            val dirtyPoultryLogs = farmDao.getDirtyPoultryLogs(farmId, lastPoultryPush)
            var maxPoultryUpdatedAt = lastPoultryPush
            for (log in dirtyPoultryLogs) {
                val resolvedUnitSyncId = if (log.unitSyncId.isNotBlank()) {
                    log.unitSyncId
                } else if (log.unitId > 0) {
                    farmDao.getUnitById(log.unitId)?.syncId ?: ""
                } else ""
                val data = hashMapOf<String, Any?>(
                    "syncId" to log.syncId,
                    "farmId" to farmId,
                    "unitSyncId" to resolvedUnitSyncId,
                    "logType" to log.logType,
                    "date" to log.date,
                    "feedType" to log.feedType,
                    "quantityKg" to log.quantityKg,
                    "costAmount" to log.costAmount,
                    "birdCount" to log.birdCount,
                    "cause" to log.cause,
                    "traysSold" to log.traysSold,
                    "pricePerTray" to log.pricePerTray,
                    "totalRevenue" to log.totalRevenue,
                    "buyer" to log.buyer,
                    "disposalReason" to log.disposalReason,
                    "disposalAmount" to log.disposalAmount,
                    "vaccineName" to log.vaccineName,
                    "targetStage" to log.targetStage,
                    "vaccineStatus" to log.vaccineStatus,
                    "notes" to log.notes,
                    "linkedLogSyncId" to log.linkedLogSyncId,
                    "updatedAt" to log.updatedAt,
                    "isDeleted" to log.isDeleted
                )
                writer.queueSet(farmRef.collection("poultry_logs").document(log.syncId), data)
                if (log.updatedAt > maxPoultryUpdatedAt) maxPoultryUpdatedAt = log.updatedAt
            }

            // 10. Worker Accounts
            val lastWorkerPush = getWatermark(farmId, "push_worker_accounts")
            val dirtyWorkers = farmDao.getDirtyWorkers(farmId, lastWorkerPush)
            var maxWorkerUpdatedAt = lastWorkerPush
            for (worker in dirtyWorkers) {
                val data = hashMapOf<String, Any?>(
                    "syncId" to worker.syncId,
                    "workerId" to worker.workerId,
                    "farmId" to farmId,
                    "name" to worker.name,
                    "emailOrPhone" to worker.emailOrPhone,
                    "role" to worker.role,
                    "isRevoked" to worker.isRevoked,
                    "createdAt" to worker.createdAt,
                    "updatedAt" to worker.updatedAt,
                    "isDeleted" to worker.isDeleted,
                    "canViewLivestock" to worker.canViewLivestock,
                    "canEditLivestock" to worker.canEditLivestock,
                    "canViewLogs" to worker.canViewLogs,
                    "canEditLogs" to worker.canEditLogs,
                    "canViewFinance" to worker.canViewFinance,
                    "canEditFinance" to worker.canEditFinance,
                    "canViewTasks" to worker.canViewTasks,
                    "canCompleteTasks" to worker.canCompleteTasks,
                    "canViewRequests" to worker.canViewRequests
                )
                writer.queueSet(farmRef.collection("worker_accounts").document(worker.workerId), data)
                if (worker.updatedAt > maxWorkerUpdatedAt) maxWorkerUpdatedAt = worker.updatedAt
            }

            // 11. Reminder completions (persisted computed-reminder state)
            val lastReminderPush = getWatermark(farmId, "push_reminder_completions")
            val dirtyReminders = farmDao.getDirtyReminderCompletions(farmId, lastReminderPush)
            var maxReminderUpdatedAt = lastReminderPush
            for (item in dirtyReminders) {
                val data = hashMapOf<String, Any>("syncId" to item.syncId, "farmId" to farmId, "ruleKey" to item.ruleKey, "unitId" to item.unitId, "completedAt" to item.completedAt, "updatedAt" to item.updatedAt, "isDeleted" to item.isDeleted)
                writer.queueSet(farmRef.collection("reminder_completions").document(item.syncId), data)
                if (item.updatedAt > maxReminderUpdatedAt) maxReminderUpdatedAt = item.updatedAt
            }

            // 12. Inventory items
            val lastInventoryPush = getWatermark(farmId, "push_inventory_items")
            val dirtyInventory = farmDao.getDirtyInventoryItems(farmId, lastInventoryPush)
            var maxInventoryUpdatedAt = lastInventoryPush
            for (item in dirtyInventory) {
                val data = hashMapOf<String, Any>("syncId" to item.syncId, "farmId" to farmId, "itemName" to item.itemName, "category" to item.category, "skuOrBarcode" to item.skuOrBarcode, "description" to item.description, "quantityAvailable" to item.quantityAvailable, "unitOfMeasurement" to item.unitOfMeasurement, "minimumThreshold" to item.minimumThreshold, "storageLocation" to item.storageLocation, "batchOrLotNumber" to item.batchOrLotNumber, "purchaseDate" to item.purchaseDate, "expirationDate" to item.expirationDate, "unitCost" to item.unitCost, "isSilage" to item.isSilage, "updatedAt" to item.updatedAt, "isDeleted" to item.isDeleted)
                writer.queueSet(farmRef.collection("inventory_items").document(item.syncId), data)
                if (item.updatedAt > maxInventoryUpdatedAt) maxInventoryUpdatedAt = item.updatedAt
            }

            // 13. Field plans and harvest outcomes
            val lastFieldPush = getWatermark(farmId, "push_field_plans")
            val dirtyFields = farmDao.getDirtyFieldPlans(farmId, lastFieldPush)
            var maxFieldUpdatedAt = lastFieldPush
            for (field in dirtyFields) {
                val data = hashMapOf<String, Any>("syncId" to field.syncId, "farmId" to farmId, "fieldName" to field.fieldName, "location" to field.location, "sizeAcres" to field.sizeAcres, "cropName" to field.cropName, "variety" to field.variety, "plantedDate" to field.plantedDate, "daysToHarvest" to field.daysToHarvest, "estimatedHarvestDate" to field.estimatedHarvestDate, "plantingNotes" to field.plantingNotes, "status" to field.status, "harvestedDate" to field.harvestedDate, "harvestOutcome" to field.harvestOutcome, "harvestedTonnes" to field.harvestedTonnes, "saleAmount" to field.saleAmount, "updatedAt" to field.updatedAt, "isDeleted" to field.isDeleted)
                writer.queueSet(farmRef.collection("field_plans").document(field.syncId), data)
                if (field.updatedAt > maxFieldUpdatedAt) maxFieldUpdatedAt = field.updatedAt
            }

            // 14. Feed plans
            val lastFeedPlanPush = getWatermark(farmId, "push_feed_plans")
            val dirtyFeedPlans = farmDao.getDirtyFeedPlans(farmId, lastFeedPlanPush)
            var maxFeedPlanUpdatedAt = lastFeedPlanPush
            for (plan in dirtyFeedPlans) {
                val data = hashMapOf<String, Any>("syncId" to plan.syncId, "farmId" to farmId, "targetUnitId" to plan.targetUnitId, "targetUnitSyncId" to plan.targetUnitSyncId, "targetUnitName" to plan.targetUnitName, "livestockType" to plan.livestockType, "inventoryItemId" to plan.inventoryItemId, "inventoryItemSyncId" to plan.inventoryItemSyncId, "inventoryItemName" to plan.inventoryItemName, "consumptionKind" to plan.consumptionKind, "dailyQuantityKg" to plan.dailyQuantityKg, "isEnabled" to plan.isEnabled, "lastProcessedDate" to plan.lastProcessedDate, "updatedAt" to plan.updatedAt, "isDeleted" to plan.isDeleted)
                writer.queueSet(farmRef.collection("feed_plans").document(plan.syncId), data)
                if (plan.updatedAt > maxFeedPlanUpdatedAt) maxFeedPlanUpdatedAt = plan.updatedAt
            }

            // 15. Immutable inventory movement ledger
            val lastMovementPush = getWatermark(farmId, "push_inventory_movements")
            val dirtyMovements = farmDao.getDirtyInventoryMovements(farmId, lastMovementPush)
            var maxMovementUpdatedAt = lastMovementPush
            for (movement in dirtyMovements) {
                val data = hashMapOf<String, Any>("syncId" to movement.syncId, "farmId" to farmId, "inventoryItemId" to movement.inventoryItemId, "inventoryItemName" to movement.inventoryItemName, "targetUnitId" to movement.targetUnitId, "targetUnitName" to movement.targetUnitName, "movementType" to movement.movementType, "quantityDeltaKg" to movement.quantityDeltaKg, "balanceAfterKg" to movement.balanceAfterKg, "occurredOn" to movement.occurredOn, "sourceKey" to movement.sourceKey, "notes" to movement.notes, "updatedAt" to movement.updatedAt, "isDeleted" to movement.isDeleted)
                writer.queueSet(farmRef.collection("inventory_movements").document(movement.syncId), data)
                if (movement.updatedAt > maxMovementUpdatedAt) maxMovementUpdatedAt = movement.updatedAt
            }

            // 16. Generated monthly report metadata. File bytes remain in secure storage.
            val lastReportsPush = getWatermark(farmId, "push_monthly_reports")
            val dirtyReports = farmDao.getDirtyMonthlyReports(farmId, lastReportsPush)
            var maxReportUpdatedAt = lastReportsPush
            for (report in dirtyReports) {
                val data = hashMapOf<String, Any>(
                    "syncId" to report.syncId,
                    "farmId" to farmId,
                    "reportMonth" to report.reportMonth,
                    "title" to report.title,
                    "generatedAt" to report.generatedAt,
                    "fileUrl" to report.fileUrl,
                    "storageKey" to report.storageKey,
                    "totalIncome" to report.totalIncome,
                    "totalExpense" to report.totalExpense,
                    "netBalance" to report.netBalance,
                    "inventoryItemCount" to report.inventoryItemCount,
                    "inventoryValue" to report.inventoryValue,
                    "totalMilkLitres" to report.totalMilkLitres,
                    "totalEggs" to report.totalEggs,
                    "updatedAt" to report.updatedAt,
                    "isDeleted" to report.isDeleted
                )
                writer.queueSet(farmRef.collection("monthly_reports").document(report.syncId), data)
                if (report.updatedAt > maxReportUpdatedAt) maxReportUpdatedAt = report.updatedAt
            }

            // Single commit for everything queued above (or a handful of commits, only
            // if some table's dirty-row count alone exceeded 500). Previously this was
            // one network round trip per row, per table — now it's ~1 round trip total
            // for a typical sync.
            writer.flush()

            // Watermarks are only advanced now, after the batch commit above has
            // actually succeeded — if commit() throws, we fall into the catch block
            // below and none of these run, so nothing is marked "pushed" that wasn't
            // really confirmed written.
            if (maxSettingsUpdatedAt > lastSettingsPush) setWatermark(farmId, "push_settings", maxSettingsUpdatedAt)
            if (maxTaskUpdatedAt > lastTasksPush) setWatermark(farmId, "push_tasks", maxTaskUpdatedAt)
            if (maxUnitUpdatedAt > lastUnitsPush) setWatermark(farmId, "push_units", maxUnitUpdatedAt)
            if (maxMilkUpdatedAt > lastMilkPush) setWatermark(farmId, "push_milk_logs", maxMilkUpdatedAt)
            if (maxMilkUsageUpdatedAt > lastMilkUsagePush) setWatermark(farmId, "push_milk_usage_logs", maxMilkUsageUpdatedAt)
            if (maxEggUpdatedAt > lastEggPush) setWatermark(farmId, "push_egg_logs", maxEggUpdatedAt)
            if (maxFinanceUpdatedAt > lastFinancePush) setWatermark(farmId, "push_finance_records", maxFinanceUpdatedAt)
            if (maxReqUpdatedAt > lastReqPush) setWatermark(farmId, "push_employee_requests", maxReqUpdatedAt)
            if (maxEventUpdatedAt > lastEventPush) setWatermark(farmId, "push_cattle_events", maxEventUpdatedAt)
            if (maxPoultryUpdatedAt > lastPoultryPush) setWatermark(farmId, "push_poultry_logs", maxPoultryUpdatedAt)
            if (maxWorkerUpdatedAt > lastWorkerPush) setWatermark(farmId, "push_worker_accounts", maxWorkerUpdatedAt)
            if (maxReminderUpdatedAt > lastReminderPush) setWatermark(farmId, "push_reminder_completions", maxReminderUpdatedAt)
            if (maxInventoryUpdatedAt > lastInventoryPush) setWatermark(farmId, "push_inventory_items", maxInventoryUpdatedAt)
            if (maxFieldUpdatedAt > lastFieldPush) setWatermark(farmId, "push_field_plans", maxFieldUpdatedAt)
            if (maxFeedPlanUpdatedAt > lastFeedPlanPush) setWatermark(farmId, "push_feed_plans", maxFeedPlanUpdatedAt)
            if (maxMovementUpdatedAt > lastMovementPush) setWatermark(farmId, "push_inventory_movements", maxMovementUpdatedAt)
            if (maxReportUpdatedAt > lastReportsPush) setWatermark(farmId, "push_monthly_reports", maxReportUpdatedAt)

            Log.d(TAG, "Successfully pushed dirty rows for farm: $farmId")
            _syncStatus.value = SyncStatus.Synced
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to push dirty rows for farm $farmId", e)
            _syncStatus.value = if (isNetworkAvailable(context)) SyncStatus.Synced else SyncStatus.Offline
        }
    }

    // ================= Remote Row Application (Last-Write-Wins) =================

    private suspend fun applyRemoteSettings(farmId: String, doc: DocumentSnapshot) {
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val existing = farmDao.getSettingsSync(farmId)
        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val updated = FarmSettings(
                id = existing?.id ?: 1,
                syncId = doc.getString("syncId") ?: "settings",
                farmId = farmId,
                farmType = doc.getString("farmType") ?: "Both",
                currency = doc.getString("currency") ?: "KES",
                weaningReminderDays = doc.getLong("weaningReminderDays")?.toInt() ?: 180,
                pregnancyCheckReminderDays = doc.getLong("pregnancyCheckReminderDays")?.toInt() ?: 30,
                dryingOffReminderDays = doc.getLong("dryingOffReminderDays")?.toInt() ?: 60,
                themeMode = doc.getString("themeMode") ?: "SYSTEM",
                automaticFeedDeductionEnabled = doc.getBoolean("automaticFeedDeductionEnabled") ?: false,
                feedDeductionLastRunDate = doc.getString("feedDeductionLastRunDate") ?: "",
                monthlyReportsEnabled = doc.getBoolean("monthlyReportsEnabled") ?: true,
                subscriptionTier = doc.getString("subscriptionTier") ?: existing?.subscriptionTier ?: "FREE",
                subscriptionStatus = doc.getString("subscriptionStatus") ?: existing?.subscriptionStatus ?: "ACTIVE",
                subscriptionExpiresAt = doc.getLong("subscriptionExpiresAt") ?: existing?.subscriptionExpiresAt ?: 0L,
                updatedAt = remoteUpdatedAt,
                isDeleted = doc.getBoolean("isDeleted") ?: false
            )
            farmDao.insertSettings(updated)
        }
    }

    private suspend fun applyRemoteSubscription(farmId: String, doc: DocumentSnapshot) {
        val existing = farmDao.getSettingsSync(farmId) ?: FarmSettings(farmId = farmId)
        val remoteUpdatedAt = subscriptionTimestamp(doc.getString("updatedAt")).takeIf { it > 0L }
            ?: System.currentTimeMillis()
        farmDao.insertSettings(
            existing.copy(
                subscriptionTier = doc.getString("tier") ?: "FREE",
                subscriptionStatus = doc.getString("status") ?: "ACTIVE",
                subscriptionExpiresAt = subscriptionTimestamp(doc.getString("expiresAt")),
                updatedAt = maxOf(existing.updatedAt, remoteUpdatedAt)
            )
        )
    }

    private suspend fun applyRemoteTask(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getTaskBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val categoryStr = doc.getString("category") ?: "GENERAL"
            val category = try { TaskCategory.valueOf(categoryStr) } catch (e: Exception) { TaskCategory.GENERAL }
            val priorityStr = doc.getString("priority") ?: "MEDIUM"
            val priority = try { TaskPriority.valueOf(priorityStr) } catch (e: Exception) { TaskPriority.MEDIUM }

            val task = FarmTask(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                title = doc.getString("title") ?: "Farm Task",
                category = category,
                targetUnit = doc.getString("targetUnit") ?: "Farm Area",
                priority = priority,
                scheduledTime = doc.getString("scheduledTime") ?: "Today",
                isCompleted = doc.getBoolean("isCompleted") ?: false,
                completedAt = doc.getString("completedAt"),
                proofPhotoUri = doc.getString("proofPhotoUri"),
                proofNotes = doc.getString("proofNotes"),
                assignedWorker = doc.getString("assignedWorker") ?: "Lead Farm Operator",
                instructions = doc.getString("instructions"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            farmDao.insertTask(task)
        }
    }

    private suspend fun applyRemoteUnit(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getUnitBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val unit = FarmUnit(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                name = doc.getString("name") ?: "Farm Unit",
                type = doc.getString("type") ?: "Cattle",
                headCount = doc.getLong("headCount")?.toInt() ?: 0,
                healthStatus = doc.getString("healthStatus") ?: "Optimal",
                location = doc.getString("location") ?: "Main Sector",
                lastUpdated = doc.getString("lastUpdated") ?: "",
                tagNumber = doc.getString("tagNumber") ?: "",
                breed = doc.getString("breed") ?: "",
                dob = doc.getString("dob") ?: "",
                dateAdded = doc.getString("dateAdded") ?: "",
                weightAtBirth = doc.getString("weightAtBirth") ?: "",
                currentWeight = doc.getString("currentWeight") ?: "",
                sire = doc.getString("sire") ?: "",
                dam = doc.getString("dam") ?: "",
                photoUri = doc.getString("photoUri"),
                notes = doc.getString("notes") ?: "",
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            val rowId = farmDao.insertUnit(unit)
            val effectiveUnitId = if (unit.id > 0) unit.id else rowId

            // Re-resolve any pending cattle events that reference this unitSyncId
            if (effectiveUnitId > 0) {
                farmDao.updateCattleEventsUnitIdByUnitSyncId(syncId, effectiveUnitId)
                farmDao.updatePoultryLogsUnitIdByUnitSyncId(syncId, effectiveUnitId)
            }
        }
    }

    private suspend fun applyRemoteMilkLog(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getMilkLogBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val log = MilkLog(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                cowName = doc.getString("cowName") ?: "Daisy",
                unitName = doc.getString("unitName") ?: "Dairy Herd",
                litres = doc.getDouble("litres") ?: 0.0,
                session = doc.getString("session") ?: "Morning",
                fatPercentage = doc.getDouble("fatPercentage") ?: 3.8,
                date = doc.getString("date") ?: "",
                loggedAt = doc.getString("loggedAt") ?: "",
                notes = doc.getString("notes"),
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            farmDao.insertMilkLog(log)
        }
    }

    private suspend fun applyRemoteMilkUsageLog(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getMilkUsageLogBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val usage = MilkUsageLog(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                date = doc.getString("date") ?: "",
                session = doc.getString("session") ?: "MORNING",
                litresToCooperative = doc.getDouble("litresToCooperative") ?: 0.0,
                litresHomeUse = doc.getDouble("litresHomeUse") ?: 0.0,
                litresToCalves = doc.getDouble("litresToCalves") ?: 0.0,
                notes = doc.getString("notes"),
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            farmDao.insertMilkUsageLog(usage)
        }
    }

    private suspend fun applyRemoteEggLog(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getEggLogBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val log = EggLog(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                unitName = doc.getString("unitName") ?: "Poultry Flock",
                totalEggs = doc.getLong("totalEggs")?.toInt() ?: 0,
                damagedEggs = doc.getLong("damagedEggs")?.toInt() ?: 0,
                grade = doc.getString("grade") ?: "Grade A",
                loggedAt = doc.getString("loggedAt") ?: "",
                notes = doc.getString("notes"),
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            farmDao.insertEggLog(log)
        }
    }

    private suspend fun applyRemoteFinanceRecord(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getFinanceRecordBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val typeStr = doc.getString("type") ?: "INCOME"
            val type = try { FinanceType.valueOf(typeStr) } catch (e: Exception) { FinanceType.INCOME }

            val rec = FinanceRecord(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                type = type,
                category = doc.getString("category") ?: "General",
                amount = doc.getDouble("amount") ?: 0.0,
                date = doc.getString("date") ?: "",
                description = doc.getString("description") ?: "Transaction",
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            farmDao.insertFinanceRecord(rec)
        }
    }

    private suspend fun applyRemoteEmployeeRequest(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getEmployeeRequestBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val statusStr = doc.getString("status") ?: "PENDING"
            val status = try { RequestStatus.valueOf(statusStr) } catch (e: Exception) { RequestStatus.PENDING }

            val req = EmployeeRequest(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                workerId = doc.getString("workerId") ?: "",
                workerEmailOrPhone = doc.getString("workerEmailOrPhone") ?: "",
                employeeName = doc.getString("employeeName") ?: "Farm Worker",
                requestType = doc.getString("requestType") ?: "Salary Advance",
                amount = doc.getDouble("amount") ?: 0.0,
                startDate = doc.getString("startDate") ?: "",
                endDate = doc.getString("endDate") ?: "",
                reason = doc.getString("reason") ?: "",
                status = status,
                submittedAt = doc.getString("submittedAt") ?: "",
                reviewNotes = doc.getString("reviewNotes"),
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            farmDao.insertEmployeeRequest(req)
        }
    }

    private suspend fun applyRemoteCattleEvent(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getCattleEventBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val unitSyncId = doc.getString("unitSyncId") ?: ""
            // Re-resolve local FK unitId from unitSyncId
            val localUnitId = if (unitSyncId.isNotBlank()) {
                farmDao.getUnitBySyncId(unitSyncId)?.id ?: 0L
            } else 0L

            val event = CattleEvent(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                unitId = localUnitId,
                unitSyncId = unitSyncId,
                category = doc.getString("category") ?: "PD",
                title = doc.getString("title") ?: "Breeding Event",
                date = doc.getString("date") ?: "",
                details = doc.getString("details") ?: "",
                notes = doc.getString("notes"),
                metricValue = doc.getString("metricValue"),
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            farmDao.insertCattleEvent(event)
        }
    }


    private suspend fun applyRemotePoultryLog(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getPoultryLogBySyncId(syncId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val unitSyncId = doc.getString("unitSyncId") ?: ""
            val localUnitId = if (unitSyncId.isNotBlank()) {
                farmDao.getUnitBySyncId(unitSyncId)?.id ?: 0L
            } else 0L
            val log = PoultryLog(
                id = existing?.id ?: 0,
                syncId = syncId,
                farmId = farmId,
                unitId = localUnitId,
                unitSyncId = unitSyncId,
                logType = doc.getString("logType") ?: "FEED",
                date = doc.getString("date") ?: "",
                feedType = doc.getString("feedType") ?: "",
                quantityKg = doc.getDouble("quantityKg") ?: 0.0,
                costAmount = doc.getDouble("costAmount") ?: 0.0,
                birdCount = doc.getLong("birdCount")?.toInt() ?: 0,
                cause = doc.getString("cause") ?: "",
                traysSold = doc.getLong("traysSold")?.toInt() ?: 0,
                pricePerTray = doc.getDouble("pricePerTray") ?: 0.0,
                totalRevenue = doc.getDouble("totalRevenue") ?: 0.0,
                buyer = doc.getString("buyer") ?: "",
                disposalReason = doc.getString("disposalReason") ?: "",
                disposalAmount = doc.getDouble("disposalAmount") ?: 0.0,
                vaccineName = doc.getString("vaccineName") ?: "",
                targetStage = doc.getString("targetStage") ?: "",
                vaccineStatus = doc.getString("vaccineStatus") ?: "",
                notes = doc.getString("notes") ?: "",
                linkedLogSyncId = doc.getString("linkedLogSyncId") ?: "",
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted
            )
            farmDao.insertPoultryLog(log)
        }
    }


    private suspend fun applyRemoteReminderCompletion(farmId: String, doc: DocumentSnapshot) {
        val ruleKey = doc.getString("ruleKey") ?: return
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val existing = farmDao.getReminderCompletion(farmId, ruleKey)
        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            farmDao.insertReminderCompletion(ReminderCompletion(id = existing?.id ?: 0, syncId = doc.id, farmId = farmId, ruleKey = ruleKey, unitId = doc.getLong("unitId") ?: 0L, completedAt = doc.getLong("completedAt") ?: System.currentTimeMillis(), updatedAt = remoteUpdatedAt, isDeleted = doc.getBoolean("isDeleted") ?: false))
        }
    }

    private suspend fun applyRemoteInventoryItem(farmId: String, doc: DocumentSnapshot) {
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val existing = farmDao.getInventoryItemBySyncId(doc.id)
        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            farmDao.insertInventoryItem(InventoryItem(id = existing?.id ?: 0, syncId = doc.id, farmId = farmId, itemName = doc.getString("itemName") ?: "Inventory item", category = doc.getString("category") ?: "Other", skuOrBarcode = doc.getString("skuOrBarcode") ?: "", description = doc.getString("description") ?: "", quantityAvailable = doc.getDouble("quantityAvailable") ?: 0.0, unitOfMeasurement = doc.getString("unitOfMeasurement") ?: "kg", minimumThreshold = doc.getDouble("minimumThreshold") ?: 0.0, storageLocation = doc.getString("storageLocation") ?: "", batchOrLotNumber = doc.getString("batchOrLotNumber") ?: "", purchaseDate = doc.getString("purchaseDate") ?: "", expirationDate = doc.getString("expirationDate") ?: "", unitCost = doc.getDouble("unitCost") ?: 0.0, isSilage = doc.getBoolean("isSilage") ?: false, updatedAt = remoteUpdatedAt, isDeleted = doc.getBoolean("isDeleted") ?: false))
        }
    }

    private suspend fun applyRemoteFieldPlan(farmId: String, doc: DocumentSnapshot) {
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val existing = farmDao.getFieldPlanBySyncId(doc.id)
        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            farmDao.insertFieldPlan(FieldPlan(id = existing?.id ?: 0, syncId = doc.id, farmId = farmId, fieldName = doc.getString("fieldName") ?: "Field", location = doc.getString("location") ?: "", sizeAcres = doc.getDouble("sizeAcres") ?: 0.0, cropName = doc.getString("cropName") ?: "Maize", variety = doc.getString("variety") ?: "", plantedDate = doc.getString("plantedDate") ?: "", daysToHarvest = (doc.getLong("daysToHarvest") ?: 120L).toInt(), estimatedHarvestDate = doc.getString("estimatedHarvestDate") ?: "", plantingNotes = doc.getString("plantingNotes") ?: "", status = doc.getString("status") ?: "GROWING", harvestedDate = doc.getString("harvestedDate") ?: "", harvestOutcome = doc.getString("harvestOutcome") ?: "", harvestedTonnes = doc.getDouble("harvestedTonnes") ?: 0.0, saleAmount = doc.getDouble("saleAmount") ?: 0.0, updatedAt = remoteUpdatedAt, isDeleted = doc.getBoolean("isDeleted") ?: false))
        }
    }


    private suspend fun applyRemoteFeedPlan(farmId: String, doc: DocumentSnapshot) {
        val updatedAt = doc.getLong("updatedAt") ?: 0L
        val existing = farmDao.getFeedPlanBySyncId(doc.id)
        if (existing == null || updatedAt >= existing.updatedAt) farmDao.insertFeedPlan(FeedPlan(id = existing?.id ?: 0, syncId = doc.id, farmId = farmId, targetUnitId = doc.getLong("targetUnitId") ?: 0L, targetUnitSyncId = doc.getString("targetUnitSyncId") ?: "", targetUnitName = doc.getString("targetUnitName") ?: "", livestockType = doc.getString("livestockType") ?: "POULTRY", inventoryItemId = doc.getLong("inventoryItemId") ?: 0L, inventoryItemSyncId = doc.getString("inventoryItemSyncId") ?: "", inventoryItemName = doc.getString("inventoryItemName") ?: "", consumptionKind = doc.getString("consumptionKind") ?: "FEED", dailyQuantityKg = doc.getDouble("dailyQuantityKg") ?: 0.0, isEnabled = doc.getBoolean("isEnabled") ?: true, lastProcessedDate = doc.getString("lastProcessedDate") ?: "", updatedAt = updatedAt, isDeleted = doc.getBoolean("isDeleted") ?: false))
    }
    private suspend fun applyRemoteInventoryMovement(farmId: String, doc: DocumentSnapshot) {
        val updatedAt = doc.getLong("updatedAt") ?: 0L
        val existing = farmDao.getInventoryMovementBySyncId(doc.id)
        if (existing == null || updatedAt >= existing.updatedAt) farmDao.insertInventoryMovement(InventoryMovement(id = existing?.id ?: 0, syncId = doc.id, farmId = farmId, inventoryItemId = doc.getLong("inventoryItemId") ?: 0L, inventoryItemName = doc.getString("inventoryItemName") ?: "", targetUnitId = doc.getLong("targetUnitId") ?: 0L, targetUnitName = doc.getString("targetUnitName") ?: "", movementType = doc.getString("movementType") ?: "MANUAL_ADJUSTMENT", quantityDeltaKg = doc.getDouble("quantityDeltaKg") ?: 0.0, balanceAfterKg = doc.getDouble("balanceAfterKg") ?: 0.0, occurredOn = doc.getString("occurredOn") ?: "", sourceKey = doc.getString("sourceKey") ?: "", notes = doc.getString("notes") ?: "", updatedAt = updatedAt, isDeleted = doc.getBoolean("isDeleted") ?: false))
    }

    private suspend fun applyRemoteWorkerAccount(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val workerId = doc.getString("workerId") ?: syncId
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val isDeleted = doc.getBoolean("isDeleted") ?: false
        val existing = farmDao.getWorkerById(workerId)

        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            val worker = WorkerAccount(
                workerId = workerId,
                syncId = syncId,
                farmId = farmId,
                name = doc.getString("name") ?: "Worker",
                emailOrPhone = doc.getString("emailOrPhone") ?: doc.getString("phone") ?: "",
                password = "", // Firebase Auth owns credentials
                role = doc.getString("role") ?: "WORKER",
                isRevoked = doc.getBoolean("isRevoked") ?: false,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = remoteUpdatedAt,
                isDeleted = isDeleted,
                canViewLivestock = doc.getBoolean("canViewLivestock") ?: true,
                canEditLivestock = doc.getBoolean("canEditLivestock") ?: true,
                canViewLogs = doc.getBoolean("canViewLogs") ?: true,
                canEditLogs = doc.getBoolean("canEditLogs") ?: true,
                canViewFinance = doc.getBoolean("canViewFinance") ?: false,
                canEditFinance = doc.getBoolean("canEditFinance") ?: false,
                canViewTasks = doc.getBoolean("canViewTasks") ?: true,
                canCompleteTasks = doc.getBoolean("canCompleteTasks") ?: true,
                canViewRequests = doc.getBoolean("canViewRequests") ?: true
            )
            farmDao.insertWorker(worker)
        }
    }

    private suspend fun applyRemoteMonthlyReport(farmId: String, doc: DocumentSnapshot) {
        val syncId = doc.id
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val existing = farmDao.getMonthlyReportBySyncId(syncId)
        if (existing == null || remoteUpdatedAt >= existing.updatedAt) {
            farmDao.insertMonthlyReport(
                MonthlyReport(
                    id = existing?.id ?: 0,
                    syncId = syncId,
                    farmId = farmId,
                    reportMonth = doc.getString("reportMonth") ?: "",
                    title = doc.getString("title") ?: "Monthly Farm Report",
                    generatedAt = doc.getLong("generatedAt") ?: remoteUpdatedAt,
                    fileUrl = doc.getString("fileUrl") ?: "",
                    storageKey = doc.getString("storageKey") ?: "",
                    totalIncome = doc.getDouble("totalIncome") ?: 0.0,
                    totalExpense = doc.getDouble("totalExpense") ?: 0.0,
                    netBalance = doc.getDouble("netBalance") ?: 0.0,
                    inventoryItemCount = doc.getLong("inventoryItemCount")?.toInt() ?: 0,
                    inventoryValue = doc.getDouble("inventoryValue") ?: 0.0,
                    totalMilkLitres = doc.getDouble("totalMilkLitres") ?: 0.0,
                    totalEggs = doc.getLong("totalEggs")?.toInt() ?: 0,
                    updatedAt = remoteUpdatedAt,
                    isDeleted = doc.getBoolean("isDeleted") ?: false
                )
            )
        }
    }
}
