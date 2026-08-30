package com.example.data

/**
 * Represents the current synchronization and connectivity state with Firestore.
 */
sealed interface SyncStatus {
    /**
     * The device has no active internet connection or Firestore is unreachable.
     * All changes are saved safely to the local Room database and cached offline.
     */
    data object Offline : SyncStatus

    /**
     * The app is currently uploading local dirty records or downloading remote updates.
     */
    data object Syncing : SyncStatus

    /**
     * All local changes are fully synchronized with Firestore and the live listener is active.
     */
    data object Synced : SyncStatus
}
