package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SyncStatus

/**
 * An unobtrusive, animated banner displayed across the top of screens when the device
 * is offline or actively syncing changes back to the cloud.
 */
@Composable
fun SyncStatusBanner(
    status: SyncStatus,
    onRetrySync: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = status !is SyncStatus.Synced,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        val (bannerBg, text, icon, iconTint) = when (status) {
            SyncStatus.Offline -> {
                Quad(
                    Color(0xFF991B1B), // Dark amber-red
                    "Working offline — all records saved locally and will auto-sync when online",
                    Icons.Filled.CloudOff,
                    Color(0xFFFCA5A5)
                )
            }
            SyncStatus.Syncing -> {
                Quad(
                    Color(0xFF1E40AF), // Dark blue
                    "Syncing changes with cloud database...",
                    Icons.Filled.Sync,
                    Color(0xFF93C5FD)
                )
            }
            SyncStatus.Synced -> {
                Quad(
                    Color(0xFF166534),
                    "All changes synced",
                    Icons.Filled.Sync,
                    Color(0xFF86EFAC)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBg)
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("sync_status_banner"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            if (status is SyncStatus.Offline) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onRetrySync() }
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Retry Sync",
                        tint = Color(0xFFFEF08A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Retry",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFEF08A)
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val a: A,
    val b: B,
    val c: C,
    val d: D
)
