package com.neilb.synapcart.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.ui.components.BrandLockup
import com.neilb.synapcart.ui.components.brandGradient
import com.neilb.synapcart.ui.components.ChatBubble
import com.neilb.synapcart.ui.components.ConfirmDialog
import com.neilb.synapcart.ui.components.LoadingSkeleton
import com.neilb.synapcart.ui.components.PlanBadge
import com.neilb.synapcart.ui.components.ProductCard
import com.neilb.synapcart.ui.components.ProductCardSkeleton
import com.neilb.synapcart.ui.components.QuotaBanner
import com.neilb.synapcart.ui.components.SectionHeader
import com.neilb.synapcart.ui.components.StatusIndicator
import com.neilb.synapcart.ui.components.SynapScaffold
import com.neilb.synapcart.ui.components.SynapTopBar
import com.neilb.synapcart.ui.components.TextInputDialog
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors
import com.neilb.synapcart.util.SessionGroup
import com.neilb.synapcart.util.sessionGroupOf
import kotlinx.coroutines.launch

/** The backend caps a chat message at 1000 characters. */
private const val MAX_MESSAGE_LENGTH = 1000

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProduct: (product: ProductDTO, siblings: List<ProductDTO>) -> Unit,
    onOpenPaywall: () -> Unit
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val currentChatTitle by viewModel.currentChatTitle.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val agentPhase by viewModel.agentPhase.collectAsStateWithLifecycle()
    val pendingProducts by viewModel.pendingProducts.collectAsStateWithLifecycle()
    val favoriteLinks by viewModel.favoriteLinks.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isLoadingMessages by viewModel.isLoadingMessages.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val quota by viewModel.quota.collectAsStateWithLifecycle()
    val isProcessing = agentPhase != null

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    LaunchedEffect(Unit) {
        viewModel.paywallEvents.collect { onOpenPaywall() }
    }

    LaunchedEffect(messages.size, agentPhase, pendingProducts.size) {
        val count = gridState.layoutInfo.totalItemsCount
        if (count > 0) gridState.animateScrollToItem(count - 1)
    }

    fun closeDrawerThen(action: () -> Unit) {
        scope.launch { drawerState.close() }
        action()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawer(
                sessions = sessions,
                currentSessionId = currentSessionId,
                isPremium = isPremium,
                onNewChat = { closeDrawerThen(viewModel::resetChat) },
                onSelect = { session -> closeDrawerThen { viewModel.loadSession(session) } },
                onRename = viewModel::renameSession,
                onDelete = viewModel::deleteSession,
                onFavorites = { closeDrawerThen(onNavigateToFavorites) },
                onProfile = { closeDrawerThen(onNavigateToProfile) },
                onLogout = { closeDrawerThen { viewModel.logout(onLogoutComplete = onLogout) } },
                onUpgrade = { closeDrawerThen(onOpenPaywall) }
            )
        }
    ) {
        SynapScaffold(
            topBar = {
                SynapTopBar(
                    centered = true,
                    titleContent = {
                        val title = currentChatTitle
                        if (title == null) {
                            BrandLockup()
                        } else {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.cd_open_menu))
                        }
                    },
                    actions = {
                        if (currentChatTitle != null) {
                            IconButton(onClick = viewModel::resetChat) {
                                Icon(Icons.Outlined.AddComment, contentDescription = stringResource(R.string.chat_new))
                            }
                        } else {
                            PlanBadge(
                                isPremium = isPremium,
                                modifier = Modifier
                                    .padding(end = Dimens.SpaceM)
                                    .clickable(enabled = !isPremium, onClick = onOpenPaywall)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                        .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
                ) {
                    quota?.let { q ->
                        QuotaBanner(used = q.used, limit = q.limit, onUpgrade = onOpenPaywall)
                    }
                    ChatInput(
                        value = inputText,
                        onValueChange = { if (it.length <= MAX_MESSAGE_LENGTH) viewModel.onInputTextChanged(it) },
                        isProcessing = isProcessing,
                        onSend = viewModel::handleSendMessage
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    isLoadingMessages && messages.isEmpty() -> LoadingSkeleton(
                        modifier = Modifier.padding(Dimens.SpaceL),
                        lines = 5
                    )
                    messages.isEmpty() -> WelcomeContent(
                        userName = userName,
                        onSuggestion = viewModel::sendSuggestion
                    )
                    else -> ChatGrid(
                        state = gridState,
                        messages = messages,
                        agentStep = agentPhase?.toStep(),
                        pendingProducts = pendingProducts,
                        favoriteLinks = favoriteLinks,
                        currency = currency,
                        onProductClick = onNavigateToProduct,
                        onToggleFavorite = viewModel::toggleFavorite
                    )
                }
            }
        }
    }
}

private fun AgentPhase.toStep() = when (this) {
    AgentPhase.SENDING, AgentPhase.ROUTING -> 0
    AgentPhase.SEARCHING -> 1
    AgentPhase.THINKING -> 2
}

@Composable
private fun ChatGrid(
    state: androidx.compose.foundation.lazy.grid.LazyGridState,
    messages: List<com.neilb.synapcart.domain.model.ChatMessage>,
    agentStep: Int?,
    pendingProducts: List<ProductDTO>,
    favoriteLinks: Set<String>,
    currency: String,
    onProductClick: (ProductDTO, List<ProductDTO>) -> Unit,
    onToggleFavorite: (ProductDTO) -> Unit
) {
    val fullSpan: (androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope.() -> GridItemSpan) = { GridItemSpan(maxLineSpan) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        messages.forEach { message ->
            if (message.text.isNotBlank()) {
                item(key = message.id, span = fullSpan) {
                    ChatBubble(
                        text = message.text,
                        isUser = message.isUser,
                        modifier = Modifier.padding(top = if (message.isUser) Dimens.SpaceM else Dimens.SpaceXs)
                    )
                }
            }
            items(message.products.size, key = { "${message.id}-$it" }) { index ->
                val product = message.products[index]
                ProductCard(
                    product = product,
                    currency = currency,
                    isFavorite = product.link != null && product.link in favoriteLinks,
                    onClick = { onProductClick(product, message.products) },
                    onToggleFavorite = { onToggleFavorite(product) }
                )
            }
        }
        if (agentStep != null) {
            item(key = "status", span = fullSpan) {
                StatusIndicator(step = agentStep, modifier = Modifier.padding(top = Dimens.SpaceXs))
            }
            if (pendingProducts.isNotEmpty()) {
                items(pendingProducts, key = { "pending-${it.link ?: it.title}" }) { product ->
                    ProductCard(
                        product = product,
                        currency = currency,
                        isFavorite = product.link != null && product.link in favoriteLinks,
                        onClick = { onProductClick(product, pendingProducts) },
                        onToggleFavorite = { onToggleFavorite(product) }
                    )
                }
            } else if (agentStep == 1) {
                items(2, key = { "skeleton-$it" }) { ProductCardSkeleton() }
            }
        }
    }
}

private data class Suggestion(val icon: ImageVector, val text: Int)

private val SUGGESTIONS = listOf(
    Suggestion(Icons.Outlined.Laptop, R.string.suggestion_laptop),
    Suggestion(Icons.Outlined.Headphones, R.string.suggestion_headphones),
    Suggestion(Icons.Outlined.PhoneAndroid, R.string.suggestion_phone),
    Suggestion(Icons.Outlined.Watch, R.string.suggestion_watch)
)

@Composable
private fun WelcomeContent(userName: String, onSuggestion: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceXl),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        Text(
            text = if (userName.isBlank()) stringResource(R.string.dashboard_hello) else stringResource(R.string.dashboard_hello_name, userName.substringBefore(' ')),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.brandGradient()
        )
        Text(
            stringResource(R.string.dashboard_prompt),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Dimens.SpaceXl))
        SectionHeader(stringResource(R.string.dashboard_try))
        SUGGESTIONS.forEach { suggestion ->
            val text = stringResource(suggestion.text)
            Surface(
                onClick = { onSuggestion(text) },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceL),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(suggestion.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimens.IconM - 2.dp))
                    Spacer(Modifier.width(Dimens.SpaceM))
                    Text(text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    isProcessing: Boolean,
    onSend: () -> Unit
) {
    val canSend = value.isNotBlank() && !isProcessing
    val placeholder = stringResource(R.string.chat_input_placeholder)
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = Dimens.SpaceL, end = Dimens.SpaceXs, top = Dimens.SpaceXs, bottom = Dimens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Dimens.SpaceM)
                    .semantics { contentDescription = placeholder },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send, capitalization = KeyboardCapitalization.Sentences),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    inner()
                }
            )
            IconButton(onClick = onSend, enabled = canSend) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .alpha(if (canSend) 1f else 0.35f)
                        .background(
                            if (canSend) SynapColors.BrandGradient else SolidColor(MaterialTheme.colorScheme.surfaceContainerHighest),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.cd_send),
                        tint = if (canSend) SynapColors.OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Dimens.IconS + 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatDrawer(
    sessions: List<ChatSessionItem>,
    currentSessionId: Int?,
    isPremium: Boolean,
    onNewChat: () -> Unit,
    onSelect: (ChatSessionItem) -> Unit,
    onRename: (Int, String) -> Unit,
    onDelete: (Int) -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit,
    onUpgrade: () -> Unit
) {
    var renameTarget by remember { mutableStateOf<ChatSessionItem?>(null) }
    var deleteTarget by remember { mutableStateOf<ChatSessionItem?>(null) }
    val untitled = stringResource(R.string.chat_untitled)

    renameTarget?.let { target ->
        TextInputDialog(
            title = stringResource(R.string.chat_rename_title),
            label = stringResource(R.string.chat_rename_label),
            initialValue = target.title.orEmpty(),
            confirmLabel = stringResource(R.string.action_save),
            onConfirm = { onRename(target.id, it); renameTarget = null },
            onDismiss = { renameTarget = null }
        )
    }
    deleteTarget?.let { target ->
        ConfirmDialog(
            title = stringResource(R.string.chat_delete_title),
            message = stringResource(R.string.chat_delete_message, target.title ?: untitled),
            confirmLabel = stringResource(R.string.action_delete),
            destructive = true,
            onConfirm = { onDelete(target.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = MaterialTheme.shapes.extraLarge.copy(topStart = androidx.compose.foundation.shape.CornerSize(0.dp), bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp)),
        modifier = Modifier.width(312.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpaceXl, vertical = Dimens.SpaceL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandLockup(modifier = Modifier.weight(1f))
            PlanBadge(isPremium = isPremium, modifier = Modifier.clickable(enabled = !isPremium, onClick = onUpgrade))
        }
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.chat_new), style = MaterialTheme.typography.labelLarge) },
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
            selected = false,
            onClick = onNewChat,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(Modifier.height(Dimens.SpaceS))

        val grouped = remember(sessions) { sessions.groupBy { sessionGroupOf(it.createdAt) }.toSortedMap() }
        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
            if (sessions.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.chat_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Dimens.SpaceXl, vertical = Dimens.SpaceM)
                    )
                }
            }
            grouped.forEach { (group, items) ->
                item(key = "header-$group") {
                    SectionHeader(
                        stringResource(group.label()),
                        modifier = Modifier.padding(start = Dimens.SpaceXl, top = Dimens.SpaceM, bottom = Dimens.SpaceXs)
                    )
                }
                items(items.size, key = { items[it].id }) { index ->
                    val session = items[index]
                    SessionRow(
                        title = session.title ?: untitled,
                        selected = session.id == currentSessionId,
                        onClick = { onSelect(session) },
                        onRename = { renameTarget = session },
                        onDelete = { deleteTarget = session }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = Dimens.SpaceS))
        DrawerLink(Icons.Outlined.FavoriteBorder, stringResource(R.string.favorites_title), onFavorites)
        DrawerLink(Icons.Outlined.PersonOutline, stringResource(R.string.profile_title), onProfile)
        DrawerLink(Icons.AutoMirrored.Outlined.Logout, stringResource(R.string.action_logout), onLogout, danger = true)
        Spacer(Modifier.height(Dimens.SpaceM))
    }
}

private fun SessionGroup.label() = when (this) {
    SessionGroup.TODAY -> R.string.group_today
    SessionGroup.YESTERDAY -> R.string.group_yesterday
    SessionGroup.LAST_7_DAYS -> R.string.group_last_7_days
    SessionGroup.LAST_30_DAYS -> R.string.group_last_30_days
    SessionGroup.OLDER -> R.string.group_older
}

@Composable
private fun SessionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    NavigationDrawerItem(
        label = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium) },
        icon = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(Dimens.IconS)) },
        selected = selected,
        onClick = onClick,
        badge = {
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(Dimens.TouchTarget)) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.cd_chat_options, title))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename)) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = { menuOpen = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = Dimens.SpaceM).height(Dimens.TouchTarget + 4.dp)
    )
}

@Composable
private fun DrawerLink(icon: ImageVector, label: String, onClick: () -> Unit, danger: Boolean = false) {
    val color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    NavigationDrawerItem(
        label = { Text(label, color = color) },
        icon = { Icon(icon, contentDescription = null, tint = if (danger) color else MaterialTheme.colorScheme.onSurfaceVariant) },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
