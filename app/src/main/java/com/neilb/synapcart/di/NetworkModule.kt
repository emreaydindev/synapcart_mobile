package com.neilb.synapcart.di

import android.content.Context
import com.neilb.synapcart.data.remote.AuthApiService
import com.neilb.synapcart.data.remote.FavoritesApiService
import com.neilb.synapcart.data.remote.SynapCartApiService
import com.neilb.synapcart.data.remote.UserApiService
import com.neilb.synapcart.data.remote.interceptor.AuthInterceptor
import com.neilb.synapcart.util.Constants
import com.neilb.synapcart.util.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private lateinit var retrofit: Retrofit

    fun init(context: Context) {
        val sessionManager = SessionManager(context)
        val authInterceptor = AuthInterceptor(sessionManager)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val apiService: SynapCartApiService by lazy {
        retrofit.create(SynapCartApiService::class.java)
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val favoritesApiService: FavoritesApiService by lazy {
        retrofit.create(FavoritesApiService::class.java)
    }
}