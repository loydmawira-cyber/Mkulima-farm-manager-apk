package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.FarmSettings
import java.util.concurrent.atomic.AtomicInteger

enum class NotificationType(
    val channelId: String,
    val channelName: String,
    val channelDescription: String,
    val defaultTitle: String
) {
    MILK_LOG(
        channelId = "mkulima_production",
        channelName = "Milk & Production Logs",
        channelDescription = "Alerts for milk collections, distribution, and egg harvests",
        defaultTitle = "Milk & Production Log"
    ),
    NEW_ENTRY(
        channelId = "mkulima_activity",
        channelName = "New Farm Entries",
        channelDescription = "Alerts when new animals, flocks, crops, inventory, or tasks are registered",
        defaultTitle = "New Farm Entry"
    ),
    ACCOUNT_CHANGE(
        channelId = "mkulima_changes",
        channelName = "Account & Settings Changes",
        channelDescription = "Alerts when farm profile, animals, workers, or settings are modified",
        defaultTitle = "Account & Farm Update"
    ),
    DELETION(
        channelId = "mkulima_deletions",
        channelName = "Deletions & Removals",
        channelDescription = "High-priority alerts when animals, logs, records, or inventory are deleted",
        defaultTitle = "Record Removed"
    ),
    REMINDER(
        channelId = "mkulima_task_reminders",
        channelName = "Task & Health Reminders",
        channelDescription = "Reminders for pending tasks, vaccinations, and breeding schedules",
        defaultTitle = "Task & Health Reminder"
    ),
    SYSTEM_ALERT(
        channelId = "farm_alerts",
        channelName = "Farm Alerts & Warnings",
        channelDescription = "System notifications, low stock warnings, and monthly reports",
        defaultTitle = "Farm Alert"
    )
}

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private const val PREFS_NAME = "mkulima_notification_prefs"
    private val notificationIdCounter = AtomicInteger(1000)

    const val CHANNEL_ID = "mkulima_task_reminders"
    private const val CHANNEL_NAME = "Task Reminders"
    private const val CHANNEL_DESCRIPTION = "Reminders for tasks due on your farm"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            NotificationType.values().forEach { type ->
                val channel = NotificationChannel(
                    type.channelId,
                    type.channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = type.channelDescription
                    enableVibration(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    // Backward compatibility alias
    fun createChannel(context: Context) {
        createChannels(context)
    }

    fun isSystemPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun canNotify(context: Context, type: NotificationType, settings: FarmSettings? = null): Boolean {
        // 1. Check system notification permission
        if (!isSystemPermissionGranted(context)) {
            Log.d(TAG, "Notification blocked: system permission not granted.")
            return false
        }

        // 2. Check preferences (settings entity priority, fallback to SharedPreferences)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val masterEnabled = settings?.notificationsEnabled ?: prefs.getBoolean("notifications_enabled", true)
        if (!masterEnabled) {
            Log.d(TAG, "Notification blocked: master notifications disabled.")
            return false
        }

        val categoryAllowed = when (type) {
            NotificationType.MILK_LOG -> settings?.notifyMilkLogs ?: prefs.getBoolean("notify_milk_logs", true)
            NotificationType.NEW_ENTRY -> settings?.notifyNewEntries ?: prefs.getBoolean("notify_new_entries", true)
            NotificationType.ACCOUNT_CHANGE -> settings?.notifyAccountChanges ?: prefs.getBoolean("notify_account_changes", true)
            NotificationType.DELETION -> settings?.notifyDeletions ?: prefs.getBoolean("notify_deletions", true)
            NotificationType.REMINDER -> settings?.notifyReminders ?: prefs.getBoolean("notify_reminders", true)
            NotificationType.SYSTEM_ALERT -> true
        }

        if (!categoryAllowed) {
            Log.d(TAG, "Notification blocked: category ${type.name} is disabled by user.")
        }
        return categoryAllowed
    }

    fun cacheNotificationPreferences(context: Context, settings: FarmSettings) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("notifications_enabled", settings.notificationsEnabled)
                .putBoolean("notify_milk_logs", settings.notifyMilkLogs)
                .putBoolean("notify_new_entries", settings.notifyNewEntries)
                .putBoolean("notify_account_changes", settings.notifyAccountChanges)
                .putBoolean("notify_deletions", settings.notifyDeletions)
                .putBoolean("notify_reminders", settings.notifyReminders)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache notification preferences", e)
        }
    }

    fun postNotification(
        context: Context,
        type: NotificationType,
        title: String,
        message: String,
        notificationId: Int = notificationIdCounter.incrementAndGet(),
        settings: FarmSettings? = null
    ) {
        if (!canNotify(context, type, settings)) return

        createChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_NOTIFICATION_TYPE", type.name)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = R.drawable.ic_notification

        val notification = NotificationCompat.Builder(context, type.channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title.ifBlank { type.defaultTitle })
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d(TAG, "Successfully posted notification [$type]: $title")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Cannot post notification without permission", e)
        } catch (e: Throwable) {
            Log.e(TAG, "Error posting notification", e)
        }
    }

    fun showTaskReminder(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        postNotification(
            context = context,
            type = NotificationType.REMINDER,
            title = title,
            message = message,
            notificationId = notificationId
        )
    }

    fun notify(type: NotificationType, title: String, message: String) {
        try {
            val app = com.example.MkulimaApplication.instance
            postNotification(
                context = app,
                type = type,
                title = title,
                message = message
            )
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to send notification via Application instance: ${e.message}")
        }
    }

    fun sendTestNotification(context: Context, settings: FarmSettings? = null) {
        postNotification(
            context = context,
            type = NotificationType.SYSTEM_ALERT,
            title = "🔔 Mkulima Farm Alert Active",
            message = "Push notifications are working! You will receive live alerts for milk entries, new records, edits, and deletions.",
            notificationId = 99999,
            settings = settings
        )
    }
}

