package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
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

/**
 * Visual badge indicating whether the app is in Offline mode, actively Syncing with Firestore,
 * or Fully Synced.
 */
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
        SyncStatus.Offline -> {
            Tuple5(
                Color(0xFFFEF2F2), // Light warm red
                Color(0xFFDC2626), // Vivid red
                Color(0xFFFCA5A5), // Red border
                "Offline Mode",
                Icons.Filled.CloudOff
            )
        }
        SyncStatus.Syncing -> {
            Tuple5(
                Color(0xFFEFF6FF), // Soft blue
                Color(0xFF2563EB), // Vibrant blue
                Color(0xFF93C5FD), // Blue border
                "Syncing...",
                Icons.Filled.Sync
            )
        }
        SyncStatus.Synced -> {
            Tuple5(
                Color(0xFFF0FDF4), // Soft emerald
                Color(0xFF16A34A), // Emerald green
                Color(0xFF86EFAC), // Green border
                "Synced",
                Icons.Filled.CloudDone
            )
        }
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
                    color = contentColor
                )
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
