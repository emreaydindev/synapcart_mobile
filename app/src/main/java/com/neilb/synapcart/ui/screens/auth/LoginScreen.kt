package com.neilb.synapcart.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapCartTextField
import com.neilb.synapcart.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsState()

    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) {
            viewModel.resetSuccessState()
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "AI-Powered Shopping Agent",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 48.dp)
        )

        SynapCartTextField(
            value = email,
            onValueChange = viewModel::onEmailChange,
            label = "E-posta",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SynapCartTextField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            label = "Şifre",
            leadingIcon = Icons.Default.Lock,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text(text = "Şifremi Unuttum", color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Giriş Yap",
            onClick = { viewModel.login() },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            Text(
                text = "VEYA",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Misafir Olarak Devam Et",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Hesabın yok mu?",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Kayıt Ol",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}