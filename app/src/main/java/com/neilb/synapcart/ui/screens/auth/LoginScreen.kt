package com.neilb.synapcart.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapCartTextField
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.theme.SynapDarkBg
import com.neilb.synapcart.ui.theme.SynapForeground

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
            .background(SynapDarkBg)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "SynapCart Marka Logosu",
            modifier = Modifier.size(130.dp)
        )

        Text(
            text = "SynapCart",
            fontSize = 42.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            color = SynapForeground,
            letterSpacing = 4.sp
        )

        Text(
            text = "Yapay Zeka Destekli Alışveriş Ajanı",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
            letterSpacing = 1.5.sp
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
                Text(
                    text = "Şifremi Unuttum",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "Giriş Yap",
            onClick = { viewModel.login() },
            isLoading = isLoading
        )

        /*
        //TODO: guest sessions will be added later
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
            Text(
                text = "VEYA",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier
                .getModifierOrEmpty()
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SynapForeground),
            border = BorderStroke(1.5.dp, SynapForeground.copy(alpha = 0.8f))
        ) {
            Text(
                text = "Misafir Olarak Devam Et",
                color = SynapForeground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
         */

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = "Hesabın yok mu? ",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 15.sp
            )
            TextButton(
                onClick = onNavigateToRegister,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Kayıt Ol",
                    color = SynapForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

private fun Modifier.getModifierOrEmpty(): Modifier = this