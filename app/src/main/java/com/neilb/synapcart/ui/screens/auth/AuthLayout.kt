package com.neilb.synapcart.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.neilb.synapcart.ui.components.SynapScaffold
import com.neilb.synapcart.ui.components.SynapTopBar
import com.neilb.synapcart.ui.theme.Dimens

/**
 * Shared frame of the auth screens: optional back button, scrollable content that stays
 * above the keyboard, centered and width-capped on large screens.
 */
@Composable
fun AuthLayout(
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    SynapScaffold(
        topBar = { if (onBack != null) SynapTopBar(onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = Dimens.ContentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceXl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
                content = content
            )
        }
    }
}
