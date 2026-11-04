package com.neilb.synapcart.data.billing

import com.neilb.synapcart.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The signed-in user's plan as last reported by `/user/me` (or a store verification endpoint). */
data class PlanState(
    val userId: String? = null,
    val isPremium: Boolean = false,
    val expiresAt: String? = null,
    /** "google" | "apple": the store that manages the active subscription. */
    val source: String? = null,
    val usageToday: Int? = null,
    val dailyLimit: Int? = null
)

/** In-memory plan state the UI observes; [SessionManager.isPremium] persists the flag across restarts. */
class UserPlanRepository {
    private val _state = MutableStateFlow(PlanState())
    val state: StateFlow<PlanState> = _state.asStateFlow()

    fun update(profile: UserProfile) {
        _state.value = PlanState(
            userId = profile.id,
            isPremium = profile.isPremium,
            expiresAt = profile.planExpiresAt,
            source = profile.planSource,
            usageToday = profile.usageToday,
            dailyLimit = profile.dailyLimit
        )
    }

    fun clear() {
        _state.value = PlanState()
    }
}
