package com.eseka.physiquest.app.chat.domain

import com.aallam.openai.api.audio.AudioResponseFormat
import com.eseka.physiquest.app.chat.domain.models.ChatPreference
import com.eseka.physiquest.app.chat.domain.models.Message
import com.eseka.physiquest.app.chat.domain.utils.getCurrentLanguage
import com.eseka.physiquest.core.domain.utils.DataError
import com.eseka.physiquest.core.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface AiDataSource {
    suspend fun chatWithAi(
        prompt: List<Message>,
        preference: ChatPreference
    ): Result<Flow<String>, DataError.Remote>

    suspend fun generateImage(prompt: String): Result<List<String>, DataError.Remote>
    suspend fun transcribeAudio(
        audioPath: String,
        language: String = getCurrentLanguage(),
        responseFormat: AudioResponseFormat = AudioResponseFormat.Text
    ): Result<String, DataError.Remote>

    suspend fun getTrendingSearches(): Result<List<String>, DataError.Remote>
    suspend fun generateChatTitle(chatContent: String): Result<String, DataError.Remote>
}