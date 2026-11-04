package com.neilb.synapcart.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.components.BrandHeader
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapTextField
import com.neilb.synapcart.ui.theme.Dimens

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) {
            viewModel.resetSuccessState()
            onLoginSuccess()
        }
    }

    AuthLayout {
        Spacer(Modifier.height(Dimens.SpaceXxl))
        BrandHeader()
        Spacer(Modifier.height(Dimens.SpaceXl))

        SynapTextField(
            value = email,
            onValueChange = viewModel::onEmailChange,
            label = stringResource(R.string.field_email),
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )
        SynapTextField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.field_password),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.login() })
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text(stringResource(R.string.login_forgot_password), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        PrimaryButton(
            text = stringResource(R.string.login_submit),
            onClick = { focusManager.clearFocus(); viewModel.login() },
            isLoading = isLoading
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_no_account), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onNavigateToRegister) {
                Text(stringResource(R.string.login_go_register), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
