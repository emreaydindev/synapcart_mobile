package com.neilb.synapcart.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.components.BrandHeader
import com.neilb.synapcart.ui.components.EmptyState
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapTextField
import com.neilb.synapcart.ui.theme.Dimens

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    AuthLayout(onBack = onNavigateBack) {
        if (isSuccess) {
            Spacer(Modifier.height(Dimens.SpaceXxl))
            EmptyState(
                icon = Icons.Outlined.MarkEmailRead,
                title = stringResource(R.string.forgot_sent_title),
                message = stringResource(R.string.forgot_sent_message, email),
                action = { PrimaryButton(text = stringResource(R.string.forgot_back_to_login), onClick = onNavigateBack) }
            )
        } else {
            BrandHeader(
                title = stringResource(R.string.forgot_title),
                subtitle = stringResource(R.string.forgot_subtitle),
                markWidth = 64.dp
            )
            Spacer(Modifier.height(Dimens.SpaceS))
            SynapTextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.field_email),
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Send,
                keyboardActions = KeyboardActions(onSend = { focusManager.clearFocus(); viewModel.sendResetLink() })
            )
            PrimaryButton(
                text = stringResource(R.string.forgot_submit),
                onClick = { focusManager.clearFocus(); viewModel.sendResetLink() },
                isLoading = isLoading
            )
        }
    }
}
