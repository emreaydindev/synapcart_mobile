package com.neilb.synapcart.di

import android.content.Context
import com.neilb.synapcart.data.billing.BillingRepository
import com.neilb.synapcart.data.billing.UserPlanRepository
import com.neilb.synapcart.data.push.PushRepository
import com.neilb.synapcart.data.remote.SynapCartApi
import com.neilb.synapcart.shared.SynapCart
import com.neilb.synapcart.domain.use_case.chat.CompareProductsUseCase
import com.neilb.synapcart.domain.use_case.favorites.SetPriceAlertUseCase
import com.neilb.synapcart.util.NotificationPermissionController
import dagger.hilt.android.qualifiers.ApplicationContext
import com.neilb.synapcart.data.repository.ChatStreamRepository
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.domain.repository.FavoritesRepository
import com.neilb.synapcart.domain.repository.SynapCartRepository
import com.neilb.synapcart.domain.repository.UserRepository
import com.neilb.synapcart.domain.use_case.auth.AuthUseCases
import com.neilb.synapcart.domain.use_case.auth.ForgotPasswordUseCase
import com.neilb.synapcart.domain.use_case.auth.LoginUseCase
import com.neilb.synapcart.domain.use_case.auth.RegisterUseCase
import com.neilb.synapcart.domain.use_case.auth.ResetPasswordUseCase
import com.neilb.synapcart.domain.use_case.chat.ChatUseCases
import com.neilb.synapcart.domain.use_case.chat.CreateSessionUseCase
import com.neilb.synapcart.domain.use_case.chat.DeleteSessionUseCase
import com.neilb.synapcart.domain.use_case.chat.GetMessagesUseCase
import com.neilb.synapcart.domain.use_case.chat.GetSessionsUseCase
import com.neilb.synapcart.domain.use_case.chat.RenameSessionUseCase
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.domain.use_case.favorites.RefreshFavoritesUseCase
import com.neilb.synapcart.domain.use_case.favorites.RemoveFavoriteUseCase
import com.neilb.synapcart.domain.use_case.favorites.ToggleFavoriteUseCase
import com.neilb.synapcart.domain.use_case.user.ChangePasswordUseCase
import com.neilb.synapcart.domain.use_case.user.DeleteAccountUseCase
import com.neilb.synapcart.domain.use_case.user.GetUserUseCase
import com.neilb.synapcart.domain.use_case.user.UpdateProfileUseCase
import com.neilb.synapcart.domain.use_case.user.UserUseCases
import com.neilb.synapcart.util.SelectedProductStore
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.SnackbarController
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
    fun provideAuthRepository(sdk: SynapCart): AuthRepository = sdk.auth

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
    fun provideChatRepository(sdk: SynapCart): SynapCartRepository = sdk.chat

    @Provides
    @Singleton
    fun provideChatUseCases(repository: SynapCartRepository): ChatUseCases {
        return ChatUseCases(
            getSessions = GetSessionsUseCase(repository),
            createSession = CreateSessionUseCase(repository),
            renameSession = RenameSessionUseCase(repository),
            deleteSession = DeleteSessionUseCase(repository),
            getMessages = GetMessagesUseCase(repository),
            compareProducts = CompareProductsUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideFavoritesRepository(sdk: SynapCart): FavoritesRepository = sdk.favorites

    @Provides
    @Singleton
    fun provideFavoritesUseCases(repository: FavoritesRepository): FavoritesUseCases {
        return FavoritesUseCases(
            refresh = RefreshFavoritesUseCase(repository),
            toggle = ToggleFavoriteUseCase(repository),
            remove = RemoveFavoriteUseCase(repository),
            setPriceAlert = SetPriceAlertUseCase(repository),
            repository = repository
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(sdk: SynapCart): UserRepository = sdk.user

    @Provides
    @Singleton
    fun provideUserPlanRepository(sdk: SynapCart): UserPlanRepository = sdk.plan

    @Provides
    @Singleton
    fun provideBillingRepository(
        @ApplicationContext context: Context,
        billingApi: SynapCartApi,
        userRepository: UserRepository,
        planRepository: UserPlanRepository,
        sessionManager: SessionManager
    ): BillingRepository = BillingRepository(context, billingApi, userRepository, planRepository, sessionManager)

    @Provides
    @Singleton
    fun providePushRepository(
        @ApplicationContext context: Context,
        userApi: SynapCartApi,
        sessionManager: SessionManager
    ): PushRepository = PushRepository(context, userApi, sessionManager)

    @Provides
    @Singleton
    fun provideNotificationPermissionController(
        @ApplicationContext context: Context,
        sessionManager: SessionManager
    ): NotificationPermissionController = NotificationPermissionController(context, sessionManager)

    @Provides
    @Singleton
    fun provideUserUseCases(repository: UserRepository): UserUseCases {
        return UserUseCases(
            getUser = GetUserUseCase(repository),
            updateProfile = UpdateProfileUseCase(repository),
            changePassword = ChangePasswordUseCase(repository),
            deleteAccount = DeleteAccountUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideSnackbarController(): SnackbarController {
        return SnackbarController()
    }

    @Provides
    @Singleton
    fun provideSelectedProductStore(): SelectedProductStore = SelectedProductStore()

    @Provides
    @Singleton
    fun provideChatStreamRepository(sdk: SynapCart): ChatStreamRepository = sdk.stream
}
