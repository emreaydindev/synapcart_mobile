package com.neilb.synapcart.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.BuildConfig
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.components.BrandHeader
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapTextField
import com.neilb.synapcart.util.PasswordRules

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRegisterSuccess by viewModel.isRegisterSuccess.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isRegisterSuccess) {
        if (isRegisterSuccess) {
            viewModel.resetSuccessState()
            onRegisterSuccess()
        }
    }

    val passwordError = password.takeIf { it.isNotEmpty() }?.let { PasswordRules.validate(it) }

    AuthLayout(onBack = onNavigateToLogin) {
        BrandHeader(
            title = stringResource(R.string.register_title),
            subtitle = stringResource(R.string.register_subtitle),
            markWidth = 64.dp
        )
        SynapTextField(
            value = fullName,
            onValueChange = viewModel::onFullNameChange,
            label = stringResource(R.string.field_full_name),
            leadingIcon = Icons.Outlined.Person
        )
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
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.register() }),
            isError = passwordError != null,
            supportingText = stringResource(passwordError ?: R.string.password_hint)
        )
        PrimaryButton(
            text = stringResource(R.string.register_submit),
            onClick = { focusManager.clearFocus(); viewModel.register() },
            isLoading = isLoading
        )
        LegalConsent()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.register_have_account), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onNavigateToLogin) {
                Text(stringResource(R.string.login_submit), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** "By signing up you accept the Terms and the Privacy Policy", with both as links. */
@Composable
private fun LegalConsent() {
    val terms = stringResource(R.string.legal_terms)
    val privacy = stringResource(R.string.legal_privacy)
    val template = stringResource(R.string.register_consent, terms, privacy)
    val linkStyle = TextLinkStyles(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold))
    val text = buildAnnotatedString {
        append(template)
        listOf(terms to "terms", privacy to "privacy").forEach { (label, path) ->
            val start = template.indexOf(label)
            if (start >= 0) {
                addLink(LinkAnnotation.Url(BuildConfig.WEB_URL + path, linkStyle), start, start + label.length)
            }
        }
    }
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
