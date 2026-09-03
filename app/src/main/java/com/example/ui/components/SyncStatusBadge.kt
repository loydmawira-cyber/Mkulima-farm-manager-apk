package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SyncStatus

@Composable
fun SyncStatusBadge(
    status: SyncStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showLabel: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_spin"
    )

    val (backgroundColor, contentColor, borderColor, text, icon) = when (status) {
        SyncStatus.Offline -> Tuple5(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFFFCA5A5), "Offline Mode", Icons.Filled.CloudOff)
        SyncStatus.Syncing -> Tuple5(Color(0xFFEFF6FF), Color(0xFF2563EB), Color(0xFF93C5FD), "Syncing...", Icons.Filled.Sync)
        SyncStatus.Synced -> Tuple5(Color(0xFFF0FDF4), Color(0xFF16A34A), Color(0xFF86EFAC), "Synced", Icons.Filled.CloudDone)
        is SyncStatus.Error -> Tuple5(Color(0xFFFFF7ED), Color(0xFFC2410C), Color(0xFFFDBA74), status.message, Icons.Filled.ErrorOutline)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("sync_status_badge"),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier
                    .size(15.dp)
                    .then(if (status == SyncStatus.Syncing) Modifier.rotate(rotationAngle) else Modifier)
            )
            if (showLabel) {
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = text,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Persistent, full-width banner for surfacing sync/connectivity state in the
 * main app body. Auto-hides when [status] is [SyncStatus.Synced]; shows a
 * retry action for Offline/Error states.
 */
@Composable
fun SyncStatusBanner(
    status: SyncStatus,
    modifier: Modifier = Modifier,
    onRetrySync: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_banner_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_banner_spin"
    )

    AnimatedVisibility(
        visible = status != SyncStatus.Synced,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val (backgroundColor, contentColor, borderColor, text, icon) = when (status) {
            SyncStatus.Offline -> Tuple5(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFFFCA5A5), "You're offline. Changes will sync once you're back online.", Icons.Filled.CloudOff)
            SyncStatus.Syncing -> Tuple5(Color(0xFFEFF6FF), Color(0xFF2563EB), Color(0xFF93C5FD), "Syncing your data...", Icons.Filled.Sync)
            SyncStatus.Synced -> Tuple5(Color(0xFFF0FDF4), Color(0xFF16A34A), Color(0xFF86EFAC), "Synced", Icons.Filled.CloudDone)
            is SyncStatus.Error -> Tuple5(Color(0xFFFFF7ED), Color(0xFFC2410C), Color(0xFFFDBA74), status.message, Icons.Filled.ErrorOutline)
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("sync_status_banner"),
            shape = RoundedCornerShape(0.dp),
            color = backgroundColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = contentColor,
                        modifier = Modifier
                            .size(16.dp)
                            .then(if (status == SyncStatus.Syncing) Modifier.rotate(rotationAngle) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 2
                    )
                }

                if (onRetrySync != null && (status == SyncStatus.Offline || status is SyncStatus.Error)) {
                    IconButton(
                        onClick = onRetrySync,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Retry sync",
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
