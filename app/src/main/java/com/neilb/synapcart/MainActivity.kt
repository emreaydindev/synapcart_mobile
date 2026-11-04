package com.neilb.synapcart

import android.content.Intent
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

    /** Holds the system splash until the start destination is known (cold start only). */
    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) keepSplash = false
        splash.setKeepOnScreenCondition { keepSplash }

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
                        onStartDestinationResolved = { keepSplash = false }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
