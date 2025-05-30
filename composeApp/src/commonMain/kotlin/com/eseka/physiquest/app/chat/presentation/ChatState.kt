package com.eseka.physiquest.app.chat.presentation

import androidx.compose.runtime.Immutable
import com.eseka.physiquest.app.chat.domain.models.Chat
import com.eseka.physiquest.app.chat.domain.models.ChatPreference
import com.eseka.physiquest.app.chat.domain.models.ChatSummary
import com.eseka.physiquest.core.presentation.UiText

@Immutable
data class ChatListState(
    val name: String = "",
    val profilePictureUrl: String? = null,
    val chats: List<ChatSummary> = emptyList(),
    val isChatListLoading: Boolean = false,
    val isChatDeleting: Boolean = false,
    val selectedChat: ChatState? = null
)

@Immutable
data class ChatState(
    val chat: Chat = Chat(id = "", title = "", messages = emptyList(), createdAt = 0L),
    val chatPreference: ChatPreference = ChatPreference(
        isReasoningEnabled = false,
        isOnlineSearchEnabled = false
    ),
    val isAudioRecording: Boolean = false,
    val audioPath: String? = null,
    val shouldGenerateImage: Boolean = false,
    val currentInput: String = "",
    val isChatLoading: Boolean = false,
    val isTranscriptionLoading: Boolean = false,
    val isTyping: Boolean = false,
    val imageCompressionError: UiText? = null,
    val messageLengthError: UiText? = null,
    val trendingTopics: List<String> = emptyList(),
    val isTrendingLoading: Boolean = false,
)