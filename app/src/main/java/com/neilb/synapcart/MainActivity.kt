package com.neilb.synapcart

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.push.PushRepository
import com.neilb.synapcart.domain.repository.FavoritesRepository
import com.neilb.synapcart.ui.screens.favorites.FavoritesViewModel.Companion.toProduct
import com.neilb.synapcart.util.NotificationPermissionController
import com.neilb.synapcart.util.PriceAlertNotifications
import kotlinx.coroutines.launch
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.neilb.synapcart.ui.components.LocalSnackbarHostState
import com.neilb.synapcart.ui.navigation.SynapCartNavHost
import com.neilb.synapcart.ui.theme.SynapCartTheme
import com.neilb.synapcart.util.SelectedProductStore
import com.neilb.synapcart.util.SessionEvents
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** AppCompatActivity so AppCompatDelegate.setApplicationLocales works on API < 33. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var snackbarController: SnackbarController

    @Inject
    lateinit var sessionEvents: SessionEvents

    @Inject
    lateinit var selectedProductStore: SelectedProductStore

    @Inject
    lateinit var favoritesRepository: FavoritesRepository

    @Inject
    lateinit var notificationPermission: NotificationPermissionController

    @Inject
    lateinit var pushRepository: PushRepository

    /** Product link from a tapped price-alert notification, until the NavHost opens it. */
    private var pendingAlertLink by mutableStateOf<String?>(null)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) lifecycleScope.launch { pushRepository.syncToken() }
        }

    /** Holds the system splash until the start destination is known (cold start only). */
    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) keepSplash = false
        splash.setKeepOnScreenCondition { keepSplash }
        if (savedInstanceState == null) pendingAlertLink = PriceAlertNotifications.linkFrom(intent)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationPermission.requests.collect {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            SynapCartTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    snackbarController.snackbarEvent.collect { message ->
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(message = message.resolve(this@MainActivity))
                    }
                }

                // No root Scaffold: every screen owns its insets and hosts the snackbar itself.
                CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                    SynapCartNavHost(
                        navController = navController,
                        sessionEvents = sessionEvents,
                        selectedProductStore = selectedProductStore,
                        onStartDestinationResolved = { keepSplash = false },
                        pendingProductLink = pendingAlertLink,
                        resolveProduct = ::resolveAlertProduct,
                        onPendingProductHandled = { pendingAlertLink = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PriceAlertNotifications.linkFrom(intent)?.let { pendingAlertLink = it }
    }

    /** The favorite behind a price alert (fresh from the server, so the new price shows). */
    private suspend fun resolveAlertProduct(link: String): ProductDTO {
        favoritesRepository.refresh()
        return favoritesRepository.favorites.value.firstOrNull { it.productLink == link }?.toProduct()
            ?: ProductDTO(title = null, price = null, source = null, link = link, thumbnail = null)
    }
}
