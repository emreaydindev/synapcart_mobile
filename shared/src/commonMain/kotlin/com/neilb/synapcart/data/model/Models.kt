package com.neilb.synapcart.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DTOs mirroring the backend's JSON (synapcart_backend src/routes/*). Nullable fields default
// to null so a missing key never fails decoding; nulls are still encoded explicitly, which
// `target_price: null` (clear a price alert) relies on.

// --- Agent stream ---

/**
 * One SSE `data:` event from `POST /chat/agent/{id}/stream`.
 * status: routing → searching → thinking (+products) → completed (+analysis, products);
 * or out_of_scope (+analysis) / error (+analysis). Only final events carry analysis.
 */
@Serializable
data class AgentStreamEvent(
    @SerialName("analysis") val analysis: String? = null,
    @SerialName("products") val products: List<ProductDTO>? = null,
    @SerialName("status") val status: String? = null
) {
    val isFinal: Boolean get() = status in FINAL_STATUSES

    companion object {
        const val ROUTING = "routing"
        const val SEARCHING = "searching"
        const val THINKING = "thinking"
        const val COMPLETED = "completed"
        const val OUT_OF_SCOPE = "out_of_scope"
        const val ERROR = "error"
        private val FINAL_STATUSES = setOf(COMPLETED, OUT_OF_SCOPE, ERROR)
    }
}

// --- Auth ---

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class RegisterRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("name") val fullName: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("user_name") val userName: String? = null
)

@Serializable
data class ForgotPasswordRequest(@SerialName("email") val email: String)

@Serializable
data class ResetPasswordRequest(
    @SerialName("token") val token: String,
    @SerialName("new_password") val newPassword: String
)

@Serializable
data class MessageResponseBody(@SerialName("message") val message: String? = null)

// --- Chat ---

@Serializable
data class ProductDTO(
    @SerialName("title") val title: String? = null,
    @SerialName("price") val price: Double? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("link") val link: String? = null,
    @SerialName("thumbnail") val thumbnail: String? = null
)

@Serializable
data class ChatRequest(@SerialName("message") val message: String)

@Serializable
data class ChatMessageResponse(
    @SerialName("id") val id: Int,
    @SerialName("content") val content: String? = null,
    @SerialName("role") val role: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("session_id") val sessionId: Int,
    @SerialName("products") val products: List<ProductDTO>? = null
)

@Serializable
data class SessionResponse(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("user_id") val userId: String,
    /** Free plan: older than the 10 newest sessions; opening it returns 402 `history_locked`. */
    @SerialName("locked") val locked: Boolean? = null
)

@Serializable
data class SessionUpdateRequest(@SerialName("title") val title: String)

@Serializable
data class CompareProduct(
    @SerialName("title") val title: String,
    @SerialName("price") val price: Double? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("link") val link: String? = null,
    @SerialName("thumbnail") val thumbnail: String? = null
)

@Serializable
data class CompareRequest(@SerialName("products") val products: List<CompareProduct>)

@Serializable
data class CompareItem(
    @SerialName("title") val title: String? = null,
    @SerialName("link") val link: String? = null,
    @SerialName("pros") val pros: List<String>? = null,
    @SerialName("cons") val cons: List<String>? = null
)

@Serializable
data class CompareResponse(
    @SerialName("items") val items: List<CompareItem>? = null,
    @SerialName("verdict") val verdict: String? = null,
    @SerialName("best_index") val bestIndex: Int? = null
)

// --- Favorites ---

@Serializable
data class AddFavoriteRequest(
    @SerialName("title") val productTitle: String,
    @SerialName("link") val productLink: String,
    @SerialName("price") val price: Double? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("thumbnail") val thumbnailUrl: String? = null
)

@Serializable
data class FavoriteResponse(
    @SerialName("id") val id: Int,
    @SerialName("title") val productTitle: String? = null,
    @SerialName("link") val productLink: String? = null,
    @SerialName("price") val price: Double? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("thumbnail") val thumbnailUrl: String? = null,
    @SerialName("added_at") val addedAt: String? = null,
    /** Price alert target (premium); null = no alert. */
    @SerialName("target_price") val targetPrice: Double? = null,
    @SerialName("last_checked_price") val lastCheckedPrice: Double? = null
)

/** Encoded with the null kept: `target_price: null` clears the alert. */
@Serializable
data class FavoriteUpdateRequest(@SerialName("target_price") val targetPrice: Double?)

// --- User ---

@Serializable
data class UserProfileUpdateRequest(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("currency") val currency: String? = null
)

@Serializable
data class UserProfile(
    @SerialName("id") val id: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("email") val email: String,
    @SerialName("language") val language: String? = null,
    @SerialName("currency") val currency: String? = null,
    // Effective plan ("free" | "premium"), where it was bought, and today's agent usage.
    @SerialName("plan") val plan: String? = null,
    @SerialName("plan_expires_at") val planExpiresAt: String? = null,
    /** "google" | "apple" while premium; tells the apps where the subscription is managed. */
    @SerialName("plan_source") val planSource: String? = null,
    @SerialName("usage_today") val usageToday: Int? = null,
    @SerialName("daily_limit") val dailyLimit: Int? = null
) {
    val isPremium: Boolean get() = plan == PLAN_PREMIUM

    companion object {
        const val PLAN_PREMIUM = "premium"
        const val SOURCE_GOOGLE = "google"
        const val SOURCE_APPLE = "apple"
    }
}

@Serializable
data class PasswordChangeRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String
)

@Serializable
data class AccountDeleteRequest(@SerialName("password") val password: String? = null)

// --- Billing and push ---

@Serializable
data class VerifyPurchaseRequest(
    @SerialName("purchase_token") val purchaseToken: String,
    @SerialName("product_id") val productId: String
)

/** StoreKit 2 `Transaction.jwsRepresentation`, verified by `POST /billing/apple/verify`. */
@Serializable
data class AppleVerifyRequest(@SerialName("signed_transaction") val signedTransaction: String)

@Serializable
data class DeviceTokenRequest(
    @SerialName("token") val token: String,
    /** "android" | "ios" */
    @SerialName("platform") val platform: String = "android"
)
