package com.neilb.synapcart.di

import android.content.Context
import com.neilb.synapcart.BuildConfig
import com.neilb.synapcart.data.push.PushRepository
import com.neilb.synapcart.data.remote.SynapCartApi
import com.neilb.synapcart.data.repository.PlatformHooks
import com.neilb.synapcart.shared.SynapCart
import com.neilb.synapcart.util.AppLocale
import com.neilb.synapcart.util.SESSION_DATASTORE_FILE
import com.neilb.synapcart.util.SessionEvents
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.createSessionDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Provider
import javax.inject.Singleton

/**
 * The shared KMP layer ([SynapCart]): Ktor API client, session DataStore and repositories.
 * The DataStore file is the same one the pre-KMP app used, so users stay signed in.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSynapCart(
        @ApplicationContext context: Context,
        pushRepository: Provider<PushRepository>
    ): SynapCart {
        val path = context.filesDir.resolve("datastore/$SESSION_DATASTORE_FILE").absolutePath
        val hooks = object : PlatformHooks {
            // Lazy: PushRepository itself depends on the API built here.
            override suspend fun beforeSignOut() = pushRepository.get().unregister()

            override suspend fun onProfileLoaded(language: String?) =
                withContext(Dispatchers.Main) { AppLocale.apply(language) }
        }
        return SynapCart(
            baseUrl = BuildConfig.BASE_URL,
            dataStore = createSessionDataStore(path),
            hooks = hooks,
            // Debug builds only: request lines with Authorization redacted, never bodies.
            logRequests = BuildConfig.DEBUG
        )
    }

    @Provides
    @Singleton
    fun provideSessionManager(sdk: SynapCart): SessionManager = sdk.session

    @Provides
    @Singleton
    fun provideSessionEvents(sdk: SynapCart): SessionEvents = sdk.sessionEvents

    @Provides
    @Singleton
    fun provideApi(sdk: SynapCart): SynapCartApi = sdk.api
}
