package com.neilb.synapcart.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.components.BrandHeader
import com.neilb.synapcart.ui.components.EmptyState
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapTextField
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.util.PasswordRules

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel,
    onNavigateToLogin: () -> Unit
) {
    val newPassword by viewModel.newPassword.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val passwordError = newPassword.takeIf { it.isNotEmpty() }?.let { PasswordRules.validate(it) }
    val mismatch = confirmPassword.isNotEmpty() && confirmPassword != newPassword

    AuthLayout {
        if (isSuccess) {
            Spacer(Modifier.height(Dimens.SpaceXxl))
            EmptyState(
                icon = Icons.Outlined.TaskAlt,
                title = stringResource(R.string.reset_done_title),
                message = stringResource(R.string.reset_done_message),
                action = { PrimaryButton(text = stringResource(R.string.login_submit), onClick = onNavigateToLogin) }
            )
        } else {
            Spacer(Modifier.height(Dimens.SpaceXl))
            BrandHeader(
                title = stringResource(R.string.reset_title),
                subtitle = stringResource(R.string.reset_subtitle),
                markWidth = 64.dp
            )
            Spacer(Modifier.height(Dimens.SpaceS))
            SynapTextField(
                value = newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                label = stringResource(R.string.field_new_password),
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                isError = passwordError != null,
                supportingText = stringResource(passwordError ?: R.string.password_hint)
            )
            SynapTextField(
                value = confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = stringResource(R.string.field_confirm_password),
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.resetPassword() }),
                isError = mismatch,
                supportingText = if (mismatch) stringResource(R.string.error_passwords_mismatch) else null
            )
            PrimaryButton(
                text = stringResource(R.string.reset_submit),
                onClick = { focusManager.clearFocus(); viewModel.resetPassword() },
                isLoading = isLoading
            )
        }
    }
}
