package com.eseka.physiquest.app.chat.domain

import com.eseka.physiquest.app.chat.domain.models.Chat
import com.eseka.physiquest.app.chat.domain.models.ChatSummary
import com.eseka.physiquest.core.domain.utils.FirebaseFirestoreError
import com.eseka.physiquest.core.domain.utils.Result
import dev.gitlive.firebase.auth.FirebaseUser

interface ChatDatabase {
    val user: FirebaseUser?
    suspend fun saveChat(chat: Chat): Result<Unit, FirebaseFirestoreError>
    suspend fun getChatById(chatId: String): Result<Chat, FirebaseFirestoreError>
    suspend fun getAllChats(): Result<List<ChatSummary>, FirebaseFirestoreError>
    suspend fun deleteChat(chatId: String): Result<Unit, FirebaseFirestoreError>
}