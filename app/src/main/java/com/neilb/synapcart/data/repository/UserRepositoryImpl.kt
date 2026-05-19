package com.neilb.synapcart.data.repository

import com.google.gson.Gson
import com.neilb.synapcart.data.model.ErrorResponse
import com.neilb.synapcart.data.model.UserProfile
import com.neilb.synapcart.data.model.UserProfileUpdateRequest
import com.neilb.synapcart.data.remote.UserApiService
import com.neilb.synapcart.domain.repository.UserRepository
import retrofit2.HttpException

class UserRepositoryImpl(
    private val apiService: UserApiService
) : UserRepository {

    override suspend fun updateProfile(fullName: String?, language: String?, currency: String?): Result<Unit> {
        return try {
            val request = UserProfileUpdateRequest(fullName, language, currency)
            apiService.updateProfile(request)
            Result.success(Unit)
        } catch (e: HttpException) {
            val errorJson = e.response()?.errorBody()?.string()
            val errorMessage = try {
                Gson().fromJson(errorJson, ErrorResponse::class.java).detail
            } catch (_: Exception) {
                "Sunucu ile iletişimde bir sorun oluştu."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            apiService.deleteAccount()
            Result.success(Unit)
        } catch (e: HttpException) {
            val errorJson = e.response()?.errorBody()?.string()
            val errorMessage = try {
                Gson().fromJson(errorJson, ErrorResponse::class.java).detail
            } catch (_: Exception) {
                "Sunucu ile iletişimde bir sorun oluştu."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getUserProfile()
            Result.success(response)
        } catch (e: HttpException) {
            val errorJson = e.response()?.errorBody()?.string()
            val errorMessage = try {
                Gson().fromJson(errorJson, ErrorResponse::class.java).detail
            } catch (_: Exception) {
                "Sunucu ile iletişimde bir sorun oluştu."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}