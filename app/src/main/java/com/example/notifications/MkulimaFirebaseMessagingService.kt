package com.example.notifications

import com.example.util.NotificationHelper
import com.example.util.NotificationType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MkulimaFirebaseMessagingService : FirebaseMessagingService() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FarmDeviceTokenRegistry.registerSavedOwnerDevice(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        NotificationHelper.createChannels(this)

        val title = message.notification?.title ?: message.data["title"] ?: "Mkulima alert"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new farm update."
        val categoryKey = message.data["category"] ?: message.data["type"] ?: ""

        val type = when {
            categoryKey.contains("milk", ignoreCase = true) || title.contains("milk", ignoreCase = true) || body.contains("milk", ignoreCase = true) -> NotificationType.MILK_LOG
            categoryKey.contains("delete", ignoreCase = true) || title.contains("delete", ignoreCase = true) || body.contains("deleted", ignoreCase = true) || body.contains("removed", ignoreCase = true) -> NotificationType.DELETION
            categoryKey.contains("edit", ignoreCase = true) || categoryKey.contains("account", ignoreCase = true) || title.contains("change", ignoreCase = true) || title.contains("update", ignoreCase = true) -> NotificationType.ACCOUNT_CHANGE
            categoryKey.contains("reminder", ignoreCase = true) || title.contains("reminder", ignoreCase = true) || body.contains("due", ignoreCase = true) -> NotificationType.REMINDER
            categoryKey.contains("entry", ignoreCase = true) || title.contains("added", ignoreCase = true) || body.contains("registered", ignoreCase = true) -> NotificationType.NEW_ENTRY
            else -> NotificationType.SYSTEM_ALERT
        }

        if (NotificationHelper.canNotify(this, type)) {
            val notificationId = message.messageId?.hashCode() ?: (System.currentTimeMillis() % 100000).toInt()
            NotificationHelper.postNotification(
                context = this,
                type = type,
                title = title,
                message = body,
                notificationId = notificationId
            )
        }
    }
}

