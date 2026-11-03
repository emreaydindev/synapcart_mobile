package com.neilb.synapcart.di

import android.content.Context
import com.neilb.synapcart.BuildConfig
import com.neilb.synapcart.data.remote.AuthApiService
import com.neilb.synapcart.data.remote.FavoritesApiService
import com.neilb.synapcart.data.remote.SynapCartApiService
import com.neilb.synapcart.data.remote.UserApiService
import com.neilb.synapcart.data.remote.interceptor.AuthInterceptor
import com.neilb.synapcart.util.SessionEvents
import com.neilb.synapcart.util.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideSessionEvents(): SessionEvents = SessionEvents()

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager, sessionEvents: SessionEvents): AuthInterceptor {
        return AuthInterceptor(sessionManager, sessionEvents)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            // The agent (and Render cold starts) can take a while before the first byte.
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            // Debug builds only: never log request bodies (passwords) or tokens in release.
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                    redactHeader("Authorization")
                }
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): SynapCartApiService {
        return retrofit.create(SynapCartApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoritesApiService(retrofit: Retrofit): FavoritesApiService {
        return retrofit.create(FavoritesApiService::class.java)
    }
}
