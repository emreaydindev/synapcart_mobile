package com.neilb.synapcart.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.neilb.synapcart.BuildConfig
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapCartTextField
import com.neilb.synapcart.ui.screens.dashboard.SurfaceDark
import com.neilb.synapcart.ui.theme.SynapDarkBg
import com.neilb.synapcart.ui.theme.SynapForeground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val fullName by viewModel.fullName.collectAsState()
    val language by viewModel.language.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAccountDeleted by viewModel.isAccountDeleted.collectAsState()

    val email by viewModel.email.collectAsState()
    val passwordChanged by viewModel.passwordChanged.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current

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
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; deletePassword = "" },
            title = { Text("Hesabı Sil", color = Color.White) },
            text = {
                Column {
                    Text(
                        "Hesabınız, sohbetleriniz ve favorileriniz kalıcı olarak silinecek. Bu işlem geri alınamaz. Onaylamak için şifrenizi girin.",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Şifre") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(deletePassword)
                        deletePassword = ""
                    },
                    enabled = deletePassword.isNotBlank()
                ) {
                    Text("Kalıcı Olarak Sil", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; deletePassword = "" }) {
                    Text("İptal", color = Color.White)
                }
            },
            containerColor = SurfaceDark
        )
    }

    Scaffold(
        containerColor = SynapDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Profil Düzenle", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        if (isLoading && fullName.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SynapForeground)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Profil İkonu
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(SurfaceDark, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SynapForeground, modifier = Modifier.size(45.dp))
                }

                if (email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = email, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                SynapCartTextField(
                    value = fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = "Ad Soyad",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSelectionGroup("Dil", listOf("Türkçe" to "tr", "English" to "en"), language) { viewModel.onLanguageChange(it) }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSelectionGroup(
                    "Para Birimi",
                    listOf("₺ TRY" to "TRY", "$ USD" to "USD", "€ EUR" to "EUR"),
                    currency
                ) { viewModel.onCurrencyChange(it) }

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButton(text = "Değişiklikleri Kaydet", isLoading = isLoading, onClick = { viewModel.updateProfile() })

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                ProfileLinkRow("Şifre Değiştir") { showPasswordDialog = true }
                ProfileLinkRow("Gizlilik Politikası") { uriHandler.openUri(BuildConfig.WEB_URL + "privacy") }
                ProfileLinkRow("Kullanım Koşulları") { uriHandler.openUri(BuildConfig.WEB_URL + "terms") }
                ProfileLinkRow("Destek") { uriHandler.openUri(BuildConfig.WEB_URL + "support") }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Hesabımı Sil", color = Color(0xFFEF4444), fontSize = 14.sp)
                }

                Text(
                    text = "SynapCart v${BuildConfig.VERSION_NAME}",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileSelectionGroup(title: String, options: List<Pair<String, String>>, selectedValue: String, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = title, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { (label, value) ->
                val isSelected = selectedValue == value
                Surface(
                    modifier = Modifier.weight(1f).height(48.dp).clickable { onSelect(value) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SynapForeground.copy(alpha = 0.15f) else SurfaceDark,
                    border = BorderStroke(1.dp, if (isSelected) SynapForeground else Color.White.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = label, color = if (isSelected) SynapForeground else Color.White, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileLinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (current: String, newPassword: String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val mismatch = confirm.isNotEmpty() && newPassword != confirm

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Şifre Değiştir", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PasswordField("Mevcut şifre", current) { current = it }
                PasswordField("Yeni şifre", newPassword) { newPassword = it }
                PasswordField("Yeni şifre (tekrar)", confirm) { confirm = it }
                Text(
                    text = if (mismatch) "Şifreler eşleşmiyor." else "En az 8 karakter, harf ve rakam içermeli.",
                    color = if (mismatch) Color(0xFFEF4444) else Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(current, newPassword) },
                enabled = !isLoading && current.isNotBlank() && newPassword.isNotBlank() && newPassword == confirm
            ) {
                Text("Kaydet", color = SynapForeground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal", color = Color.White) }
        },
        containerColor = SurfaceDark
    )
}

@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}
