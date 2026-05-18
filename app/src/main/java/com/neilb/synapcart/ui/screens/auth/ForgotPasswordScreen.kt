package com.neilb.synapcart.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapCartTextField

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri Dön",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isSuccess) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Başarılı",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "E-posta Gönderildi!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Şifre sıfırlama bağlantısı için lütfen $email adresinin gelen kutusunu (veya spam klasörünü) kontrol et.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            PrimaryButton(
                text = "Giriş Ekranına Dön",
                onClick = onNavigateBack
            )
        } else {
            Text(
                text = "Şifremi Unuttum",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Hesabına bağlı e-posta adresini gir, sana şifreni yenilemen için bir bağlantı gönderelim.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            SynapCartTextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                label = "E-posta",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            PrimaryButton(
                text = "Sıfırlama Bağlantısı Gönder",
                onClick = { viewModel.sendResetLink() },
                isLoading = isLoading
            )
        }
    }
}