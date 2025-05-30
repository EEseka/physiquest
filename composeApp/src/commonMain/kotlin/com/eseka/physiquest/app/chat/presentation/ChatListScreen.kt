package com.eseka.physiquest.app.chat.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.chat.presentation.components.ChatSummaryItem
import com.eseka.physiquest.core.presentation.components.ShimmerListItem
import com.eseka.physiquest.core.presentation.components.SwipeableItemWithActions
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.tap_plus_to_start
import physiquest.composeapp.generated.resources.there
import physiquest.composeapp.generated.resources.welcome_no_chats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    state: ChatListState,
    onChatClicked: (String) -> Unit,
    onDeleteChatClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Glowing animation for empty state
    val infiniteTransition = rememberInfiniteTransition()
    val glowingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val welcomeTextGlow by rememberInfiniteTransition().animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    var chatSummariesUi = remember {
        mutableStateListOf<ChatSummaryUi>()
    }

    LaunchedEffect(state.chats) {
        chatSummariesUi.clear()
        chatSummariesUi.addAll(
            state.chats.map {
                ChatSummaryUi(
                    id = it.id,
                    title = it.title,
                    updatedAt = it.updatedAt,
                    isOptionsVisible = false,
                    isDeleting = false
                )
            }
        )
    }

    if (state.isChatListLoading) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            repeat(10) {
                ShimmerListItem(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    } else if (chatSummariesUi.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = glowingAlpha),
            )
            val name =
                if (state.name.isEmpty()) stringResource(Res.string.there) else state.name
            Text(
                text = stringResource(Res.string.welcome_no_chats, name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = welcomeTextGlow),
                            MaterialTheme.colorScheme.secondary.copy(alpha = welcomeTextGlow)
                        )
                    )
                ),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.tap_plus_to_start),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 72.dp)
        ) {
            itemsIndexed(
                items = chatSummariesUi,
                key = { _, chatSummary -> chatSummary.id })
            { index, chatSummary ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SwipeableItemWithActions(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(MaterialTheme.shapes.medium),
                        isRevealed = chatSummary.isOptionsVisible,
                        onExpanded = {
                            chatSummariesUi[index] = chatSummary.copy(
                                isOptionsVisible = true
                            )
                        },
                        onCollapsed = {
                            chatSummariesUi[index] = chatSummary.copy(
                                isOptionsVisible = false
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onDeleteChatClicked(chatSummary.id)
                                    chatSummariesUi[index] = chatSummary.copy(
                                        isOptionsVisible = false,
                                        isDeleting = state.isChatDeleting
                                    )
                                    chatSummariesUi.removeAt(index)
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        },
                        content = {
                            ChatSummaryItem(
                                chat = chatSummary,
                                onClick = { onChatClicked(chatSummary.id) }
                            )
                        }
                    )
                }
            }
        }
    }
}

data class ChatSummaryUi(
    val id: String,
    val title: String,
    val updatedAt: Long,
    val isOptionsVisible: Boolean,
    val isDeleting: Boolean
)