package com.example.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.example.util.NotificationHelper

@Composable
fun RequestMkulimaNotifications() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mkulima_notification_prefs", Context.MODE_PRIVATE) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.createChannels(context)
            FarmDeviceTokenRegistry.refreshRegisteredToken(context)
        }
    }

    LaunchedEffect(Unit) {
        NotificationHelper.createChannels(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            FarmDeviceTokenRegistry.refreshRegisteredToken(context)
            return@LaunchedEffect
        }
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            FarmDeviceTokenRegistry.refreshRegisteredToken(context)
        } else if (!prefs.getBoolean("notification_permission_requested", false)) {
            prefs.edit().putBoolean("notification_permission_requested", true).apply()
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

