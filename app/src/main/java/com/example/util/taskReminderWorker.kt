package com.example.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val farmId = getActiveFarmId(context) ?: return Result.success()

            val db = AppDatabase.getInstance(context)
            val farmDao = db.farmDao()

            val todayFormats = listOf(
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )

            val tasks = farmDao.getTasksForFarmSync(farmId)
                .filter { task ->
                    !task.isCompleted &&
                        todayFormats.any { fmt -> task.dueDateStr.contains(fmt) }
                }

            tasks.forEach { task ->
                NotificationHelper.showTaskReminder(
                    context = context,
                    notificationId = task.id.hashCode(),
                    title = "Task due today",
                    message = task.title
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun getActiveFarmId(context: Context): String? {
        val prefs = context.getSharedPreferences("mkulima_session", Context.MODE_PRIVATE)
        return prefs.getString("active_farm_id", null)
    }
}
