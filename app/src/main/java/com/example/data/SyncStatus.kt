package com.example.data

/** Represents the current synchronization and connectivity state with Firestore. */
sealed interface SyncStatus {
    data object Offline : SyncStatus
    data object Syncing : SyncStatus
    data object Synced : SyncStatus
    data class Error(val message: String) : SyncStatus
}
