package com.neilb.synapcart.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.neilb.synapcart.data.model.*
import com.neilb.synapcart.data.remote.SynapCartApiService
import com.neilb.synapcart.domain.model.ChatMessage
import com.neilb.synapcart.domain.repository.SynapCartRepository
import retrofit2.HttpException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Instant

class SynapCartRepositoryImpl(
    private val apiService: SynapCartApiService
) : SynapCartRepository {

    override suspend fun sendMessage(sessionId: Int, message: String): Result<AgentResponse> {
        return try {
            val response = apiService.sendMessage(sessionId, ChatRequest(message))
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

    override suspend fun getSessions(): Result<List<SessionResponse>> {
        return try {
            val response = apiService.getSessions()
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

    override suspend fun createSession(): Result<SessionResponse> {
        return try {
            val response = apiService.createSession()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getMessages(sessionId: Int): Result<List<ChatMessage>> {
        return try {
            val response = apiService.getMessages(sessionId)
            Result.success(response.map {
                ChatMessage(
                    id = it.id.toString(),
                    text = it.content,
                    isUser = it.role != "assistant",
                    timestamp = LocalDateTime.parse(it.createdAt, DateTimeFormatter.ISO_DATE_TIME)
                        .toInstant(ZoneOffset.UTC)
                        .toEpochMilli()
                )
            })
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