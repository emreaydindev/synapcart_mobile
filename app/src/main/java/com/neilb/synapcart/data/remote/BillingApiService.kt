package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.UserProfile
import com.neilb.synapcart.data.model.VerifyPurchaseRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface BillingApiService {

    /** Validates a Play subscription purchase on the server; returns the updated profile. */
    @POST("api/v1/billing/google/verify")
    suspend fun verifyGooglePurchase(@Body request: VerifyPurchaseRequest): UserProfile
}
