package com.neilb.synapcart.di

import com.neilb.synapcart.data.remote.AuthApiService
import com.neilb.synapcart.data.remote.FavoritesApiService
import com.neilb.synapcart.data.remote.SynapCartApiService
import com.neilb.synapcart.data.remote.UserApiService
import com.neilb.synapcart.data.repository.AuthRepositoryImpl
import com.neilb.synapcart.data.repository.FavoritesRepositoryImpl
import com.neilb.synapcart.data.repository.SynapCartRepositoryImpl
import com.neilb.synapcart.data.repository.UserRepositoryImpl
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.domain.repository.FavoritesRepository
import com.neilb.synapcart.domain.repository.SynapCartRepository
import com.neilb.synapcart.domain.repository.UserRepository
import com.neilb.synapcart.domain.use_case.auth.*
import com.neilb.synapcart.domain.use_case.chat.ChatUseCases
import com.neilb.synapcart.domain.use_case.chat.CreateSessionUseCase
import com.neilb.synapcart.domain.use_case.chat.ForgotPasswordUseCase
import com.neilb.synapcart.domain.use_case.chat.GetSessionsUseCase
import com.neilb.synapcart.domain.use_case.chat.SendMessageUseCase
import com.neilb.synapcart.domain.use_case.favorites.AddFavoriteUseCase
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.domain.use_case.favorites.GetFavoritesUseCase
import com.neilb.synapcart.domain.use_case.favorites.RemoveFavoriteUseCase
import com.neilb.synapcart.domain.use_case.user.DeleteAccountUseCase
import com.neilb.synapcart.domain.use_case.user.UpdateProfileUseCase
import com.neilb.synapcart.domain.use_case.user.UserUseCases
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.SnackbarController
import com.google.gson.Gson
import com.neilb.synapcart.domain.use_case.chat.GetMessagesUseCase
import com.neilb.synapcart.domain.use_case.user.GetUserUseCase
import okhttp3.OkHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: AuthApiService, sessionManager: SessionManager): AuthRepository {
        return AuthRepositoryImpl(apiService, sessionManager)
    }

    @Provides
    @Singleton
    fun provideAuthUseCases(repository: AuthRepository): AuthUseCases {
        return AuthUseCases(
            login = LoginUseCase(repository),
            register = RegisterUseCase(repository),
            forgotPassword = ForgotPasswordUseCase(repository),
            resetPassword = ResetPasswordUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideChatRepository(apiService: SynapCartApiService): SynapCartRepository {
        return SynapCartRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideChatUseCases(repository: SynapCartRepository): ChatUseCases {
        return ChatUseCases(
            getSessions = GetSessionsUseCase(repository),
            sendMessage = SendMessageUseCase(repository),
            createSession = CreateSessionUseCase(repository),
            getMessages = GetMessagesUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideFavoritesRepository(apiService: FavoritesApiService): FavoritesRepository {
        return FavoritesRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideFavoritesUseCases(repository: FavoritesRepository): FavoritesUseCases {
        return FavoritesUseCases(
            getFavorites = GetFavoritesUseCase(repository),
            addFavorite = AddFavoriteUseCase(repository),
            removeFavorite = RemoveFavoriteUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: UserApiService): UserRepository {
        return UserRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideUserUseCases(repository: UserRepository): UserUseCases {
        return UserUseCases(
            updateProfile = UpdateProfileUseCase(repository),
            deleteAccount = DeleteAccountUseCase(repository),
            getUser = GetUserUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideSnackbarController(): SnackbarController {
        return SnackbarController()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideChatStreamRepository(okHttpClient: OkHttpClient, gson: Gson): com.neilb.synapcart.data.repository.ChatStreamRepository {
        return com.neilb.synapcart.data.repository.ChatStreamRepositoryImpl(okHttpClient, gson)
    }
}