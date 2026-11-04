package com.neilb.synapcart.shared

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.neilb.synapcart.data.billing.UserPlanRepository
import com.neilb.synapcart.data.remote.SynapCartApi
import com.neilb.synapcart.data.remote.defaultHttpEngine
import com.neilb.synapcart.data.repository.AuthRepositoryImpl
import com.neilb.synapcart.data.repository.ChatStreamRepository
import com.neilb.synapcart.data.repository.ChatStreamRepositoryImpl
import com.neilb.synapcart.data.repository.FavoritesRepositoryImpl
import com.neilb.synapcart.data.repository.NoPlatformHooks
import com.neilb.synapcart.data.repository.PlatformHooks
import com.neilb.synapcart.data.repository.SynapCartRepositoryImpl
import com.neilb.synapcart.data.repository.UserRepositoryImpl
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.domain.repository.FavoritesRepository
import com.neilb.synapcart.domain.repository.SynapCartRepository
import com.neilb.synapcart.domain.repository.UserRepository
import com.neilb.synapcart.util.SessionEvents
import com.neilb.synapcart.util.SessionManager
import io.ktor.client.engine.HttpClientEngine

/**
 * Composition root of the shared layer: one instance per app process. Android builds it in
 * Hilt (`di/NetworkModule`), iOS in `AppContainer.swift` (through [SynapCartClient]).
 */
class SynapCart(
    baseUrl: String,
    dataStore: DataStore<Preferences>,
    hooks: PlatformHooks = NoPlatformHooks,
    engine: HttpClientEngine = defaultHttpEngine(),
    logRequests: Boolean = false
) {
    val session = SessionManager(dataStore)
    val sessionEvents = SessionEvents()
    val api = SynapCartApi(baseUrl, session, sessionEvents, engine, logRequests)
    val plan = UserPlanRepository()
    val favorites: FavoritesRepository = FavoritesRepositoryImpl(api, session)
    val chat: SynapCartRepository = SynapCartRepositoryImpl(api)
    val stream: ChatStreamRepository = ChatStreamRepositoryImpl(api)
    val user: UserRepository = UserRepositoryImpl(api, session, favorites, plan, hooks)
    val auth: AuthRepository = AuthRepositoryImpl(api, session, favorites, plan, hooks)
}
