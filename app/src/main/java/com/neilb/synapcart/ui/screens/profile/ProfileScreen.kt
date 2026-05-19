package com.neilb.synapcart.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.neilb.synapcart.ui.screens.dashboard.DarkBg
import com.neilb.synapcart.ui.screens.dashboard.NeonAccent
import com.neilb.synapcart.ui.screens.dashboard.SurfaceDark

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

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isAccountDeleted) {
        if (isAccountDeleted) {
            onAccountDeleted()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hesabı Sil", fontWeight = FontWeight.Bold) },
            text = { Text("Hesabınızı kalıcı olarak silmek istediğinize emin misiniz? Bu işlem geri alınamaz ve tüm verileriniz kaybolur.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAccount()
                }) {
                    Text("Sil", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal", color = Color.White)
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Profil", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        if (isLoading && fullName.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(SurfaceDark, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(45.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                SynapCartTextField(
                    value = fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = "Ad Soyad",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(Modifier.fillMaxWidth()) {
                    Text(text = "Dil", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileOptionCard(label = "Türkçe", isSelected = language == "tr", modifier = Modifier.weight(1f)) { viewModel.onLanguageChange("tr") }
                        ProfileOptionCard(label = "English", isSelected = language == "en", modifier = Modifier.weight(1f)) { viewModel.onLanguageChange("en") }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(Modifier.fillMaxWidth()) {
                    Text(text = "Para Birimi", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileOptionCard(label = "₺ (TRY)", isSelected = currency == "TRY", modifier = Modifier.weight(1f)) { viewModel.onCurrencyChange("TRY") }
                        ProfileOptionCard(label = "$ (USD)", isSelected = currency == "USD", modifier = Modifier.weight(1f)) { viewModel.onCurrencyChange("USD") }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                PrimaryButton(
                    text = "Değişiklikleri Kaydet",
                    isLoading = isLoading,
                    onClick = { viewModel.updateProfile() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Hesabımı Sil", color = Color(0xFFEF4444), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileOptionCard(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) NeonAccent.copy(alpha = 0.1f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) NeonAccent else Color.White.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) NeonAccent else Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}