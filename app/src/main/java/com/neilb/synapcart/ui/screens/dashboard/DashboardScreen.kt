package com.neilb.synapcart.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.domain.model.ChatMessage
import kotlinx.coroutines.launch

val DarkBg = Color(0xFF030712)
val NeonAccent = Color(0xFFA4E636)
val SurfaceDark = Color(0xFF111827)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProduct: (ProductDTO) -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val currentChatTitle by viewModel.currentChatTitle.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val products by viewModel.products.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isProcessing, statusText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = DarkBg
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.resetChat()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAccent.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeni Sohbet", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Geçmiş Sohbetler",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(chatHistory) { (id, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    viewModel.loadSession(id, title)
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = title,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                onNavigateToProfile()
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Profil", color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                onNavigateToFavorites()
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Favoriler", color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                viewModel.logout(onLogoutComplete = onLogout)
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Çıkış Yap", color = Color.Red, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = DarkBg,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (currentChatTitle == null) {
                            Text(
                                text = "SynapCart",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif,
                                color = NeonAccent
                            )
                        } else {
                            Text(
                                text = currentChatTitle!!,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = viewModel::onInputTextChanged,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { if (!isProcessing && inputText.isNotBlank()) viewModel.handleSendMessage() }),
                            cursorBrush = SolidColor(NeonAccent),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text("Ajan'a sor...", color = Color.White.copy(alpha = 0.4f), fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (inputText.isNotBlank() && !isProcessing) NeonAccent else Color.Transparent,
                                    CircleShape
                                )
                                .clickable(enabled = inputText.isNotBlank() && !isProcessing, onClick = viewModel::handleSendMessage),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = NeonAccent, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Gönder",
                                    tint = if (inputText.isNotBlank()) DarkBg else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp).padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Merhaba, ${userName.ifBlank { "Misafir" }}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bugün senin için ne bulabilirim?",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.5f),
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        val suggestions = listOf(
                            "💻  Fiyat/performans laptop öner",
                            "🎧  En ucuz AirPods nerede?",
                            "📱  40.000 TL altı telefon bul"
                        )

                        suggestions.forEach { suggestion ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        viewModel.onInputTextChanged(suggestion.substring(3).trim())
                                        viewModel.handleSendMessage()
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.05f))
                            ) {
                                Text(
                                    text = suggestion,
                                    color = Color.White.copy(0.8f),
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages) { message ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (message.isUser) NeonAccent else SurfaceDark,
                                            shape = RoundedCornerShape(
                                                topStart = 20.dp,
                                                topEnd = 20.dp,
                                                bottomStart = if (message.isUser) 20.dp else 4.dp,
                                                bottomEnd = if (message.isUser) 4.dp else 20.dp
                                            )
                                        )
                                        .padding(horizontal = 18.dp, vertical = 12.dp)
                                        .widthIn(max = 290.dp)
                                ) {
                                    if (message.isUser) {
                                        Text(text = message.text, color = DarkBg, fontSize = 15.sp)
                                    } else {
                                        val annotated = remember(message.text) { parseAiStyledText(message.text) }
                                        Text(text = annotated, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
                                    }
                                }
                            }
                        }

                        if (statusText != null) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonAccent, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(text = statusText ?: "", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                                }
                            }
                        }

                        if (products.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Bulunan Ürünler", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            val productChunks = products.chunked(2)
                            items(productChunks) { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    chunk.forEach { product ->
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 4.dp)
                                                .clickable { onNavigateToProduct(product) },
                                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .height(90.dp)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color.White.copy(alpha = 0.05f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (!product.thumbnail.isNullOrBlank()) {
                                                        AsyncImage(
                                                            model = product.thumbnail,
                                                            contentDescription = product.title,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White.copy(alpha = 0.2f))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(text = product.title ?: "-", color = Color.White, maxLines = 2, fontSize = 14.sp, fontWeight = FontWeight.Medium, overflow = TextOverflow.Ellipsis)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = if (product.price != null) "${product.price} TRY" else "-", color = NeonAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    IconButton(
                                                        onClick = { viewModel.toggleFavorite(product) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = NeonAccent, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (chunk.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseAiStyledText(input: String): androidx.compose.ui.text.AnnotatedString {
    val pattern = Regex("(\\*\\*\\*(.+?)\\*\\*\\*|\\*\\*(.+?)\\*\\*|`(.+?)`)")
    val builder = buildAnnotatedString {
        var lastIndex = 0
        for (m in pattern.findAll(input)) {
            val range = m.range
            if (range.first > lastIndex) append(input.substring(lastIndex, range.first))
            val triple = m.groups[2]?.value
            val double = m.groups[3]?.value
            val code = m.groups[4]?.value
            when {
                triple != null -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) { append(triple) }
                double != null -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(double) }
                code != null -> withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace, background = Color.DarkGray)) { append(code) }
                else -> append(m.value)
            }
            lastIndex = range.last + 1
        }
        if (lastIndex < input.length) append(input.substring(lastIndex))
    }
    return builder
}