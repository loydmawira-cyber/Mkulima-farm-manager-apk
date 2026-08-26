package com.example.notifications

import android.content.Context
import com.example.data.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FarmDeviceTokenRegistry {
    private const val PREFS = "mkulima_auth_prefs"

    fun registerOwnerDevice(context: Context, session: UserSession) {
        if (!session.isOwner) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            writeToken(context, token, session.farmId, session.userId, session.role)
        }
    }

    fun registerSavedOwnerDevice(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val farmId = prefs.getString("farm_id", null) ?: return
        val userId = prefs.getString("user_id", null) ?: return
        val role = prefs.getString("user_role", "OWNER") ?: "OWNER"
        if (!role.equals("OWNER", ignoreCase = true)) return
        writeToken(context, token, farmId, userId, role)
    }

    fun refreshRegisteredToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            registerSavedOwnerDevice(context, token)
        }
    }

    private fun writeToken(context: Context, token: String, farmId: String, userId: String, role: String) {
        if (token.isBlank() || farmId.isBlank() || userId.isBlank()) return
        FirebaseFirestore.getInstance()
            .collection("farms").document(farmId)
            .collection("device_tokens").document(token)
            .set(
                mapOf(
                    "token" to token,
                    "farmId" to farmId,
                    "userId" to userId,
                    "role" to role.uppercase(),
                    "platform" to "android",
                    "updatedAt" to System.currentTimeMillis(),
                    "isDeleted" to false
                )
            )
    }
}
