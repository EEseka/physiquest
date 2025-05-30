package com.eseka.physiquest.app.chat.presentation

sealed class ChatEvents {
    data class OnDeleteChat(val chatId: String) : ChatEvents()
    data class OnChatSelected(val chatId: String) : ChatEvents()
    data object OnCreateNewChat : ChatEvents()
    data object OnMessageSent : ChatEvents()
    data class OnInputChanged(val input: String) : ChatEvents()
    data class OnTrendingTopicSelected(val topic: String) : ChatEvents()
    data class OnChatTitleChanged(val title: String) : ChatEvents()
    data object OnSaveTitleEdit : ChatEvents()
    data object OnToggleReasoning : ChatEvents()
    data object OnToggleOnlineSearch : ChatEvents()
    data object OnToggleImageGeneration : ChatEvents()
    data class OnEditMessage(val messageId: String) : ChatEvents()
    data class OnEditedMessageSent(val messageId: String) : ChatEvents()
    data object OnStartAudioRecording : ChatEvents()
    data object OnStopAudioRecording : ChatEvents()
    data object OnCancelAudioRecording : ChatEvents()
}