package com.neilb.synapcart.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.BuildConfig
import com.neilb.synapcart.R
import com.neilb.synapcart.data.billing.BillingRepository
import com.neilb.synapcart.ui.components.ChipGroup
import com.neilb.synapcart.ui.components.ConfirmDialog
import com.neilb.synapcart.ui.components.PlanBadge
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SecondaryButton
import com.neilb.synapcart.ui.components.SectionHeader
import com.neilb.synapcart.ui.components.SettingsCard
import com.neilb.synapcart.ui.components.SettingsRow
import com.neilb.synapcart.ui.components.SynapScaffold
import com.neilb.synapcart.ui.components.SynapTextField
import com.neilb.synapcart.ui.components.SynapTopBar
import com.neilb.synapcart.ui.components.currentLocale
import com.neilb.synapcart.ui.screens.onboarding.CURRENCY_OPTIONS
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors
import com.neilb.synapcart.util.AppLocale
import com.neilb.synapcart.util.PasswordRules
import com.neilb.synapcart.util.parseServerTimestamp
import java.text.DateFormat
import java.util.Date


@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isAccountDeleted by viewModel.isAccountDeleted.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val passwordChanged by viewModel.passwordChanged.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val planExpiresAt by viewModel.planExpiresAt.collectAsStateWithLifecycle()

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LaunchedEffect(isAccountDeleted) {
        if (isAccountDeleted) onAccountDeleted()
    }
    LaunchedEffect(passwordChanged) {
        if (passwordChanged) {
            showPasswordDialog = false
            viewModel.onPasswordChangeHandled()
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            isLoading = isLoading,
            onDismiss = { showPasswordDialog = false },
            onConfirm = { current, new -> viewModel.changePassword(current, new) }
        )
    }
    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = { password ->
                showDeleteDialog = false
                viewModel.deleteAccount(password)
            }
        )
    }

    SynapScaffold(
        topBar = { SynapTopBar(title = stringResource(R.string.profile_title), onBack = onNavigateBack) }
    ) { innerPadding ->
        if (isLoading && email.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@SynapScaffold
        }
        Box(
            Modifier
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
                    .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
            ) {
                ProfileHeader(name = fullName, email = email, isPremium = isPremium)

                PlanCard(
                    isPremium = isPremium,
                    expiresAt = planExpiresAt,
                    onUpgrade = onOpenPaywall,
                    onManage = { uriHandler.openUri(BillingRepository.manageSubscriptionUrl(context.packageName)) }
                )

                SectionHeader(stringResource(R.string.profile_section_settings), Modifier.padding(top = Dimens.SpaceS))
                SettingsCard {
                    Column(Modifier.padding(Dimens.SpaceL), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
                        SynapTextField(
                            value = fullName,
                            onValueChange = viewModel::onFullNameChange,
                            label = stringResource(R.string.field_full_name),
                            leadingIcon = Icons.Outlined.Person,
                            imeAction = ImeAction.Done
                        )
                        Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ChipGroup(
                            options = listOf(
                                stringResource(R.string.language_turkish) to AppLocale.TURKISH,
                                stringResource(R.string.language_english) to AppLocale.ENGLISH
                            ),
                            selected = language,
                            onSelect = viewModel::onLanguageChange
                        )
                        Text(stringResource(R.string.settings_currency), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ChipGroup(options = CURRENCY_OPTIONS, selected = currency, onSelect = viewModel::onCurrencyChange)
                        PrimaryButton(
                            text = stringResource(R.string.action_save_changes),
                            isLoading = isLoading,
                            onClick = viewModel::updateProfile
                        )
                    }
                }

                SectionHeader(stringResource(R.string.profile_section_account), Modifier.padding(top = Dimens.SpaceS))
                SettingsCard {
                    SettingsRow(stringResource(R.string.profile_change_password), Icons.Outlined.Key, { showPasswordDialog = true })
                }

                SectionHeader(stringResource(R.string.profile_section_about), Modifier.padding(top = Dimens.SpaceS))
                SettingsCard {
                    SettingsRow(stringResource(R.string.legal_privacy), Icons.Outlined.PrivacyTip, { uriHandler.openUri(BuildConfig.WEB_URL + "privacy") })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 56.dp))
                    SettingsRow(stringResource(R.string.legal_terms), Icons.Outlined.Description, { uriHandler.openUri(BuildConfig.WEB_URL + "terms") })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 56.dp))
                    SettingsRow(stringResource(R.string.profile_support), Icons.AutoMirrored.Outlined.HelpOutline, { uriHandler.openUri(BuildConfig.WEB_URL + "support") })
                }

                SettingsCard(Modifier.padding(top = Dimens.SpaceS)) {
                    SettingsRow(
                        stringResource(R.string.profile_delete_account),
                        Icons.Outlined.DeleteForever,
                        { showDeleteDialog = true },
                        tint = MaterialTheme.colorScheme.error,
                        showChevron = false
                    )
                }

                Text(
                    text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = Dimens.SpaceL)
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(name: String, email: String, isPremium: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = Dimens.SpaceS)) {
        val initials = name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
        Box(
            Modifier.size(64.dp).background(SynapColors.BrandGradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initials.ifEmpty { "?" }, style = MaterialTheme.typography.titleLarge, color = SynapColors.OnPrimary)
        }
        Spacer(Modifier.width(Dimens.SpaceL))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXxs)) {
            Text(name.ifBlank { email }, style = MaterialTheme.typography.titleLarge)
            Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        PlanBadge(isPremium)
    }
}

@Composable
private fun PlanCard(isPremium: Boolean, expiresAt: String?, onUpgrade: () -> Unit, onManage: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isPremium) SynapColors.PremiumGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Dimens.SpaceL), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(
                    Icons.Outlined.WorkspacePremium,
                    contentDescription = null,
                    tint = SynapColors.PremiumGold
                )
                Spacer(Modifier.width(Dimens.SpaceM))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(if (isPremium) R.string.plan_card_premium_title else R.string.plan_card_free_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    val expiry = parseServerTimestamp(expiresAt)?.let {
                        DateFormat.getDateInstance(DateFormat.MEDIUM, currentLocale()).format(Date(it))
                    }
                    Text(
                        when {
                            isPremium && expiry != null -> stringResource(R.string.plan_card_renews, expiry)
                            isPremium -> stringResource(R.string.plan_card_premium_body)
                            else -> stringResource(R.string.plan_card_free_body)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isPremium) {
                SecondaryButton(text = stringResource(R.string.plan_manage), onClick = onManage)
            } else {
                PrimaryButton(
                    text = stringResource(R.string.action_upgrade_premium),
                    onClick = onUpgrade,
                    brush = SynapColors.PremiumGradient,
                    contentColor = SynapColors.OnPremiumGold
                )
            }
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (current: String, newPassword: String) -> Unit
) {
    var current by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val mismatch = confirm.isNotEmpty() && newPassword != confirm
    val rule = newPassword.takeIf { it.isNotEmpty() }?.let { PasswordRules.validate(it) }

    ConfirmDialog(
        title = stringResource(R.string.profile_change_password),
        message = stringResource(R.string.password_change_message),
        confirmLabel = stringResource(R.string.action_save),
        onConfirm = { onConfirm(current, newPassword) },
        onDismiss = onDismiss,
        confirmEnabled = !isLoading && current.isNotBlank() && newPassword.isNotBlank() && rule == null && newPassword == confirm
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
            SynapTextField(current, { current = it }, stringResource(R.string.field_current_password), isPassword = true)
            SynapTextField(
                newPassword, { newPassword = it }, stringResource(R.string.field_new_password), isPassword = true,
                isError = rule != null, supportingText = stringResource(rule ?: R.string.password_hint)
            )
            SynapTextField(
                confirm, { confirm = it }, stringResource(R.string.field_confirm_password), isPassword = true,
                imeAction = ImeAction.Done,
                isError = mismatch, supportingText = if (mismatch) stringResource(R.string.error_passwords_mismatch) else null
            )
        }
    }
}

@Composable
private fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    ConfirmDialog(
        title = stringResource(R.string.profile_delete_account),
        message = stringResource(R.string.delete_account_message),
        confirmLabel = stringResource(R.string.delete_account_confirm),
        destructive = true,
        confirmEnabled = password.isNotBlank(),
        onConfirm = { onConfirm(password) },
        onDismiss = onDismiss
    ) {
        SynapTextField(password, { password = it }, stringResource(R.string.field_password), isPassword = true, imeAction = ImeAction.Done)
        Spacer(Modifier.height(0.dp))
    }
}
