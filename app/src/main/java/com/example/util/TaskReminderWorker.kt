package com.example.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.MkulimaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val farmId = getActiveFarmId(context) ?: return Result.success()

            val db = MkulimaDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
            val farmDao = db.farmDao()

            val tasks = farmDao.getTasksByFarm(farmId).first()
                .filter { task ->
                    !task.isCompleted && task.scheduledTime.startsWith("Today", ignoreCase = true)
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
        val prefs = context.getSharedPreferences("mkulima_auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("farm_id", null)
    }
}
