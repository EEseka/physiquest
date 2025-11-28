package com.eseka.physiquest.app.chat.data

import com.aallam.openai.api.audio.AudioResponseFormat
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.exception.AuthenticationException
import com.aallam.openai.api.exception.InvalidRequestException
import com.aallam.openai.api.exception.PermissionException
import com.aallam.openai.api.exception.RateLimitException
import com.aallam.openai.api.exception.UnknownAPIException
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.diamondedge.logging.logging
import com.eseka.physiquest.app.chat.data.local.TrendingSearchDao
import com.eseka.physiquest.app.chat.data.local.mappers.toTrendingSearch
import com.eseka.physiquest.app.chat.data.local.mappers.toTrendingSearchEntity
import com.eseka.physiquest.app.chat.data.mappers.toChatRole
import com.eseka.physiquest.app.chat.data.utils.safeOpenAiNetworkCall
import com.eseka.physiquest.app.chat.domain.AiDataSource
import com.eseka.physiquest.app.chat.domain.models.ChatPreference
import com.eseka.physiquest.app.chat.domain.models.Message
import com.eseka.physiquest.core.domain.services.FileManager
import com.eseka.physiquest.core.domain.services.ImageCompressor
import com.eseka.physiquest.core.domain.utils.DataError
import com.eseka.physiquest.core.domain.utils.Result
import com.eseka.physiquest.core.domain.utils.map
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.io.files.Path

class OpenAiRepository(
    private val openAI: OpenAI,
    private val dao: TrendingSearchDao,
    private val fileManager: FileManager,
    private val imageCompressor: ImageCompressor
) : AiDataSource {
    override suspend fun chatWithAi(
        prompt: List<Message>,
        preference: ChatPreference
    ): Result<Flow<String>, DataError.Remote> = try {
        val initialMessages = buildMessages(prompt, preference)

        val modelId = when {
            preference.isReasoningEnabled && preference.isOnlineSearchEnabled -> "gpt-4-turbo"
            preference.isReasoningEnabled -> "gpt-4.1-mini"
            preference.isOnlineSearchEnabled -> "gpt-4o-mini-search-preview"
            else -> "gpt-3.5-turbo"
        }

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId(modelId),
            messages = initialMessages.toList()
        )

        safeOpenAiNetworkCall {
            openAI.chatCompletions(chatCompletionRequest)
                .map { it.choices.first().delta?.content.orEmpty() }
        }

    } catch (e: RateLimitException) {
        log.e(tag = TAG, msg = { "RateLimitException: ${e.message}" })
//        Result.Success(flowOf(stringResource(Res.string.friendly_rate_limit_message)))
        Result.Success(flowOf("I'm currently experiencing high demand. Please try again in a moment."))
    } catch (e: InvalidRequestException) {
        log.e(
            tag = TAG,
            msg = { "InvalidRequestException: ${e.error.detail?.message ?: e.message}" })
//        Result.Success(flowOf(stringResource(Res.string.friendly_invalid_request_message)))
        Result.Success(flowOf("I couldn't process that request. Please try asking in a different way."))
    } catch (e: AuthenticationException) {
        log.e(tag = TAG, msg = { "AuthenticationException: ${e.message}" })
//        Result.Success(flowOf(stringResource(Res.string.friendly_service_error)))
        Result.Success(flowOf("I'm having trouble connecting to my services right now. Please try again later."))
    } catch (e: PermissionException) {
        log.e(tag = TAG, msg = { "PermissionException: ${e.message}" })
        Result.Success(flowOf("I'm having trouble connecting to my services right now. Please try again later."))
    } catch (e: UnknownAPIException) {
        log.e(tag = TAG, msg = { "UnknownAPIException: ${e.message}" })
        Result.Success(flowOf("I'm having trouble connecting to my services right now. Please try again later."))
    } catch (e: Exception) {
        log.e(tag = TAG, msg = { "Exception: ${e.message}" })
//        Result.Success(flowOf(stringResource(Res.string.error_unknown)))
        Result.Success(flowOf("Something went wrong. Please try again later."))
    }

    override suspend fun generateImage(prompt: String): Result<List<String>, DataError.Remote> =
        try {
            safeOpenAiNetworkCall {
                openAI.imageURL(
                    creation = ImageCreation(
                        prompt = prompt,
                        model = ModelId("dall-e-3"),
                        n = 1,
                        size = ImageSize.is1024x1024
                    )
                )
            }.map { images ->
                var downloadedImageUrl: String? = null
                fileManager.downloadImageFromUrl(images.first().url)?.let { byteArray ->
                    val url = fileManager.saveImageToCache(
                        byteArray,
                        "generated_image_${Clock.System.now().toEpochMilliseconds()}.png"
                    )
                    downloadedImageUrl = url
                }
                downloadedImageUrl?.let {
                    imageCompressor.compressImage(it, MAX_IMAGE_SIZE)
                        .onSuccess { compressedUri ->
                            downloadedImageUrl = fileManager.saveImageToCache(
                                compressedUri,
                                "compressed_generated_image_${
                                    Clock.System.now().toEpochMilliseconds()
                                }.png"
                            ).toString()
                        }
                        .onError { error ->
                            log.e(tag = TAG, msg = { "Error compressing image: $error" })
                        }
                }
                downloadedImageUrl?.let {
                    listOf(it)
                } ?: emptyList()
            }
        } catch (e: RateLimitException) {
            log.e(tag = TAG, msg = { "RateLimitException: ${e.message}" })
            Result.Success(emptyList())
        } catch (e: InvalidRequestException) {
            log.e(tag = TAG, msg = { "InvalidRequestException: ${e.error.detail?.message}" })
            Result.Success(emptyList())
        } catch (e: AuthenticationException) {
            log.e(tag = TAG, msg = { "AuthenticationException: ${e.message}" })
            Result.Success(emptyList())
        } catch (e: PermissionException) {
            log.e(tag = TAG, msg = { "PermissionException: ${e.message}" })
            Result.Success(emptyList())
        } catch (e: UnknownAPIException) {
            log.e(tag = TAG, msg = { "UnknownAPIException: ${e.message}" })
            Result.Success(emptyList())
        } catch (e: Exception) {
            log.e(tag = TAG, msg = { "Exception: ${e.message}" })
            Result.Success(emptyList())
        }

    override suspend fun transcribeAudio(
        audioPath: String,
        language: String,
        responseFormat: AudioResponseFormat
    ): Result<String, DataError.Remote> = try {
        val audioFileSource = FileSource(path = Path(audioPath))
        val transcriptionRequest = TranscriptionRequest(
            audio = audioFileSource,
            model = ModelId("whisper-1"),
            language = language,
            responseFormat = responseFormat
        )
        safeOpenAiNetworkCall {
            openAI.transcription(transcriptionRequest)
        }.map { transcription ->
            transcription.text
        }
    } catch (e: Exception) {
        log.e(tag = TAG, msg = { "Exception: ${e.message}" })
        Result.Success("")
    }

    override suspend fun getTrendingSearches(): Result<List<String>, DataError.Remote> {
        try {
            // Check if trending searches are already cached
            val cachedSearches = dao.getTrendingSearches()
            if (cachedSearches != null && cachedSearches.toTrendingSearch().isNotEmpty()) {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                // Check if it's been more than a day since the last update
                if (currentTime - cachedSearches.timestamp > ONE_DAY_IN_MILLIS) {
                    return fetchAndUpdateTrendingSearches()
                }
                return Result.Success(cachedSearches.toTrendingSearch())
            }
            // If not cached, fetch from OpenAI
            return fetchAndUpdateTrendingSearches()
        } catch (e: Exception) {
            log.e(tag = TAG, msg = { "Exception: ${e.message}" })
            return Result.Success(emptyList())
        }
    }

    override suspend fun generateChatTitle(chatContent: String): Result<String, DataError.Remote> =
        try {
            val messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are an assistant tasked with summarizing chat content and generating concise, descriptive titles."
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = "Create a short, descriptive title for the following chat content. Do not include quotes, punctuation, or any extra formatting: $chatContent"
                )
            )
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4-turbo"),
                messages = messages
            )
            safeOpenAiNetworkCall {
                openAI.chatCompletion(chatCompletionRequest)
            }.map { chatCompletion ->
                chatCompletion.choices.firstOrNull()?.message?.content.orEmpty().trim()
                    .removePrefix("\"").removeSuffix("\"")
            }
        } catch (e: Exception) {
            log.e(tag = TAG, msg = { "Exception: ${e.message}" })
            Result.Error(DataError.Remote.UNKNOWN)
        }

    private suspend fun fetchAndUpdateTrendingSearches(): Result<List<String>, DataError.Remote> =
        try {
            val messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are a real-time science trends analyst specializing in physics, scientific discoveries, and technology breakthroughs. Focus on what researchers, educators, and the public are actively discussing, sharing, and searching about right now in the world of science and physics."
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = "List 10 currently trending topics in physics or science that people are actively discussing today. Include recent discoveries, experiments, theories, and technology advancements. Exclude generic terms like app names, company names, or everyday searches. Provide only specific, timely topics in concise phrases without numbering, links or extra text."
                )
            )
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4o-mini-search-preview"),
                messages = messages
            )
            safeOpenAiNetworkCall {
                openAI.chatCompletion(chatCompletionRequest)
            }.map { chatCompletion ->
                val content = chatCompletion.choices.firstOrNull()?.message?.content.orEmpty()
                val formattedContent = content.lines()
                    .map {
                        it.trim().removePrefix("-").removePrefix("•").removePrefix("\"")
                            .removeSuffix("\"")
                            .replaceFirst(Regex("^\\d+\\.\\s*"), "") // Remove numbering
                    }
                    .filter { it.isNotBlank() }
                    .take(10)

                dao.clearTrendingSearches()
                dao.insertTrendingSearches(formattedContent.toTrendingSearchEntity())
                dao.getTrendingSearches()?.toTrendingSearch() ?: emptyList()
            }
        } catch (e: Exception) {
            log.e(tag = TAG, msg = { "Exception: ${e.message}" })
            Result.Success(
                listOf(
                    "Technology trends",
                    "AI advancements",
                    "Global news",
                    "Health tips",
                    "Finance markets"
                )
            )
        }

    // Helper function to build chat messages for a request.
    private fun buildMessages(
        messages: List<Message>,
        prefs: ChatPreference
    ): MutableList<ChatMessage> {
        val reasoningAddon = if (prefs.isReasoningEnabled)
            "Use scientific reasoning, mathematical proofs, and empirical evidence where appropriate. Break down complex physics concepts into understandable explanations."
        else ""

        val searchAddon = if (prefs.isOnlineSearchEnabled)
            "You can reference recent scientific papers, physics research, and verified online sources."
        else ""

        val updatedMessages = mutableListOf(
            ChatMessage(
                role = ChatRole.System,
                content = """
                You are PhysiQuest AI, a specialized physics and science education assistant. You excel in physics, 
                mathematics, and related scientific fields. Provide accurate, academically-sound explanations 
                while maintaining scientific rigor. When explaining concepts, use clear examples and, where helpful, 
                include relevant equations. Always verify scientific claims and correct misconceptions. 
                $reasoningAddon $searchAddon
            """.trimIndent()
            )
        )

        updatedMessages.addAll(
            messages.map { message ->
                ChatMessage(
                    role = message.role.toChatRole(),
                    content = message.content
                )
            }
        )
        return updatedMessages
    }

    companion object {
        private const val TAG = "OpenAiRepository"
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private const val MAX_IMAGE_SIZE = 1024 * 1024L // 1MB
        val log = logging()
    }
}


