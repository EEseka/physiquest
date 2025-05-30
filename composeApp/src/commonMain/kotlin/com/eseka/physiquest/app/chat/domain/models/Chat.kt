package com.eseka.physiquest.app.chat.domain.models

data class Chat(
    val id: String,
    val title: String,
    val messages: List<Message>,
    val createdAt: Long
)