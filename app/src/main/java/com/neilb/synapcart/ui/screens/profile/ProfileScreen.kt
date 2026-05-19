package com.neilb.synapcart.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isAccountDeleted) {
        if (isAccountDeleted) onAccountDeleted()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hesabı Sil", color = Color.White) },
            text = { Text("Hesabınızı kalıcı olarak silmek istediğinize emin misiniz? Bu işlem geri alınamaz.", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.deleteAccount() }) {
                    Text("Sil", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
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

                ProfileSelectionGroup("Para Birimi", listOf("₺ (TRY)" to "TRY", "$ (USD)" to "USD"), currency) { viewModel.onCurrencyChange(it) }

                Spacer(modifier = Modifier.weight(1f))

                PrimaryButton(text = "Değişiklikleri Kaydet", isLoading = isLoading, onClick = { viewModel.updateProfile() })

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                ) {
                    Text(text = "Hesabımı Sil", color = Color(0xFFEF4444), fontSize = 14.sp)
                }
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