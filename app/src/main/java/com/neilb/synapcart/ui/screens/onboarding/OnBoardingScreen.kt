package com.neilb.synapcart.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neilb.synapcart.ui.components.PrimaryButton

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val curr by viewModel.selectedCurrency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()

    LaunchedEffect(isComplete) {
        if (isComplete) {
            onFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Hoş Geldin!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Alışveriş deneyimini kişiselleştirelim.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(64.dp))

        SelectionSection(title = "Dil Seçimi") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OptionCard(
                    label = "Türkçe",
                    isSelected = lang == "tr",
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectLanguage("tr") }

                OptionCard(
                    label = "English",
                    isSelected = lang == "en",
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectLanguage("en") }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        SelectionSection(title = "Para Birimi") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OptionCard(
                    label = "₺ (TRY)",
                    isSelected = curr == "TRY",
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectCurrency("TRY") }

                OptionCard(
                    label = "$ (USD)",
                    isSelected = curr == "USD",
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectCurrency("USD") }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Başlayalım",
            isLoading = isLoading,
            onClick = { viewModel.finishOnboarding() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}


@Composable
fun SelectionSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun OptionCard(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}