package com.example.payments

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * Starts a Paystack checkout for the direct APK.
 *
 * The Paystack secret key never belongs in Android. The Android app sends a
 * Firebase ID token to the server, and the server verifies ownership, creates
 * the checkout, and verifies the payment again before activating access.
 */
class PaystackCheckoutClient(
    private val apiBaseUrl: String = SmartFarmApiConfig.BASE_URL,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    suspend fun initializeCheckout(
        farmId: String,
        tier: PaystackCheckoutTier
    ): Result<PaystackCheckout> = withContext(Dispatchers.IO) {
        runCatching {
            val authUser = FirebaseAuth.getInstance().currentUser
                ?: throw IllegalStateException("Please sign in again before starting checkout.")
            val token = Tasks.await(authUser.getIdToken(true))?.token
                ?: throw IllegalStateException("Your secure sign-in token is unavailable. Please sign in again.")
            val email = authUser.email?.trim()?.lowercase()
                ?: throw IllegalStateException("Add a recovery email in Settings before paying by Paystack.")
            if (!email.contains("@") || !email.contains(".")) {
                throw IllegalStateException("Add a valid recovery email in Settings before paying by Paystack.")
            }
            if (farmId.isBlank()) throw IllegalArgumentException("The farm ID is missing. Please sign in again.")

            val requestJson = JSONObject()
                .put("farmId", farmId)
                .put("tier", tier.name)
                .put("email", email)
                .toString()
            val request = Request.Builder()
                .url("${apiBaseUrl.trimEnd('/')}/api/paystack/checkout")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .post(requestJson.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = runCatching { JSONObject(body) }.getOrNull()
                if (!response.isSuccessful || json == null || json.optBoolean("ok", false).not()) {
                    val message = json?.optString("error")?.takeIf { it.isNotBlank() }
                        ?: "Unable to start Paystack checkout (${response.code})."
                    throw IOException(message)
                }
                val checkoutJson = json.optJSONObject("checkout")
                    ?: throw IOException("Paystack checkout response is incomplete.")
                val authorizationUrl = checkoutJson.optString("authorizationUrl").trim()
                if (!authorizationUrl.startsWith("https://")) {
                    throw IOException("Paystack did not return a secure checkout URL.")
                }
                PaystackCheckout(
                    authorizationUrl = authorizationUrl,
                    reference = checkoutJson.optString("reference"),
                    tier = tier,
                    amountSubunits = checkoutJson.optLong("amount"),
                    currency = checkoutJson.optString("currency", "KES")
                )
            }
        }
    }

    suspend fun verifyCheckout(
        farmId: String,
        tier: PaystackCheckoutTier,
        reference: String
    ): Result<PaystackVerificationResult> = withContext(Dispatchers.IO) {
        runCatching {
            val authUser = FirebaseAuth.getInstance().currentUser
                ?: throw IllegalStateException("Please sign in again before verifying payment.")
            val token = Tasks.await(authUser.getIdToken(true))?.token
                ?: throw IllegalStateException("Your secure sign-in token is unavailable. Please sign in again.")
            if (farmId.isBlank() || reference.isBlank()) {
                throw IllegalArgumentException("The farm or payment reference is missing.")
            }
            val requestJson = JSONObject()
                .put("farmId", farmId)
                .put("tier", tier.name)
                .put("reference", reference)
                .toString()
            val request = Request.Builder()
                .url("${apiBaseUrl.trimEnd('/')}/api/paystack/verify")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .post(requestJson.toRequestBody(JSON_MEDIA_TYPE))
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = runCatching { JSONObject(body) }.getOrNull()
                if (!response.isSuccessful || json == null || !json.optBoolean("ok", false)) {
                    val message = json?.optString("error")?.takeIf { it.isNotBlank() }
                        ?: "Payment is not confirmed yet (${response.code})."
                    throw IOException(message)
                }
                val subscription = json.optJSONObject("subscription")
                    ?: throw IOException("Verified subscription response is incomplete.")
                PaystackVerificationResult(
                    tier = subscription.optString("tier"),
                    status = subscription.optString("status"),
                    expiresAt = subscription.optString("expiresAt"),
                    reference = subscription.optString("orderId", reference)
                )
            }
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

enum class PaystackCheckoutTier {
    PREMIUM,
    PRO
}

data class PaystackCheckout(
    val authorizationUrl: String,
    val reference: String,
    val tier: PaystackCheckoutTier,
    val amountSubunits: Long,
    val currency: String
)

data class PaystackVerificationResult(
    val tier: String,
    val status: String,
    val expiresAt: String,
    val reference: String
)

object SmartFarmApiConfig {
    /** Current published server URL; change only if the server domain changes. */
    const val BASE_URL = "https://mkulimafeed-8bfp3iu3.manus.space"
}
