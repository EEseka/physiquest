package com.eseka.physiquest.app.chat.data.mappers

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.core.Role
import com.eseka.physiquest.app.chat.domain.models.Chat
import com.eseka.physiquest.app.chat.domain.models.ChatSummary
import com.eseka.physiquest.app.chat.domain.models.Message

fun ChatMessage.toMessage(id: String, timestamp: Long, image: String?): Message {
    return Message(
        id = id,
        role = role.toDomainRole(),
        content = content ?: "",
        image = image,
        timestamp = timestamp
    )
}

fun Role.toDomainRole(): DomainRole {
    return when (this) {
        Role.User -> DomainRole.USER
        Role.Assistant -> DomainRole.ASSISTANT
        Role.System -> DomainRole.ASSISTANT
        else -> {}
    } as DomainRole
}

fun DomainRole.toChatRole(): ChatRole {
    return when (this) {
        DomainRole.USER -> Role.User
        DomainRole.ASSISTANT -> Role.Assistant
        DomainRole.SYSTEM -> Role.System
    }
}

fun Chat.toChatSummary(updatedAt: Long): ChatSummary {
    return ChatSummary(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

typealias DomainRole = com.eseka.physiquest.app.chat.domain.models.Role