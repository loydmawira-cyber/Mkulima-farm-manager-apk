package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records how a milking session's milk production was split between the cooperative (sold),
 * home/household use, and calves. One row per farm, per date, per session (Morning/Afternoon/Evening).
 */
@Entity(tableName = "milk_usage_logs")
data class MilkUsageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = "",
    val farmId: String = "FARM-DEFAULT",
    val date: String = "",
    val session: String = "MORNING",
    val litresToCooperative: Double = 0.0,
    val litresHomeUse: Double = 0.0,
    val litresToCalves: Double = 0.0,
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    val totalAllocated: Double
        get() = litresToCooperative + litresHomeUse + litresToCalves
}
