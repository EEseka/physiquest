package com.eseka.physiquest.app.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import com.eseka.physiquest.app.MainEvent
import com.eseka.physiquest.app.MainEventBus
import com.eseka.physiquest.app.chat.domain.AiDataSource
import com.eseka.physiquest.app.chat.domain.ChatDatabase
import com.eseka.physiquest.app.chat.domain.models.Chat
import com.eseka.physiquest.app.chat.domain.models.Message
import com.eseka.physiquest.app.chat.domain.models.Role
import com.eseka.physiquest.core.domain.services.AudioRecorder
import com.eseka.physiquest.core.domain.services.FileManager
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import com.eseka.physiquest.core.presentation.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.message_too_long
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatViewModel(
    private val aiDataSource: AiDataSource,
    private val chatDatabase: ChatDatabase,
    private val fileManager: FileManager,
    private val audioRecorder: AudioRecorder,
    private val mainEventBus: MainEventBus
) : ViewModel() {
    private val name = chatDatabase.user?.displayName ?: ""
    private val photoUri = chatDatabase.user?.photoURL

    private val _state = MutableStateFlow(ChatListState(name = name, profilePictureUrl = photoUri))
    val state = _state
        .onStart {
            getAllChats()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000L),
            ChatListState(name = name, profilePictureUrl = photoUri)
        )

    fun onEvent(event: ChatEvents) {
        when (event) {
            is ChatEvents.OnDeleteChat -> deleteChat(event.chatId)
            is ChatEvents.OnChatSelected -> {
                loadChat(event.chatId)
                loadTrendingTopics()
            }

            ChatEvents.OnCreateNewChat -> createNewChat()
            ChatEvents.OnMessageSent -> sendMessage(_state.value.selectedChat?.currentInput!!)
            is ChatEvents.OnEditedMessageSent -> sendMessage(
                _state.value.selectedChat?.currentInput!!,
                event.messageId
            )

            is ChatEvents.OnInputChanged -> {
                _state.update { it.copy(selectedChat = it.selectedChat?.copy(currentInput = event.input)) }
            }

            is ChatEvents.OnTrendingTopicSelected -> {
                _state.update { it.copy(selectedChat = it.selectedChat?.copy(currentInput = event.topic)) }
                sendMessage(event.topic, isTrending = true)
            }

            is ChatEvents.OnChatTitleChanged -> {
                _state.update {
                    it.copy(
                        selectedChat = it.selectedChat?.copy(
                            chat = it.selectedChat.chat.copy(
                                title = event.title
                            )
                        )
                    )
                }
            }

            ChatEvents.OnSaveTitleEdit -> saveChat()
            ChatEvents.OnToggleReasoning -> {
                _state.update {
                    it.copy(
                        selectedChat = it.selectedChat?.copy(
                            chatPreference = it.selectedChat.chatPreference.copy(
                                isReasoningEnabled = !it.selectedChat.chatPreference.isReasoningEnabled
                            )
                        )
                    )
                }
            }

            ChatEvents.OnToggleOnlineSearch -> {
                _state.update {
                    it.copy(
                        selectedChat = it.selectedChat?.copy(
                            chatPreference = it.selectedChat.chatPreference.copy(
                                isOnlineSearchEnabled = !it.selectedChat.chatPreference.isOnlineSearchEnabled
                            )
                        )
                    )
                }
            }

            ChatEvents.OnToggleImageGeneration -> {
                _state.update {
                    it.copy(
                        selectedChat = it.selectedChat?.copy(
                            shouldGenerateImage = !it.selectedChat.shouldGenerateImage
                        )
                    )
                }
            }

            is ChatEvents.OnEditMessage -> editMessage(event.messageId)
            ChatEvents.OnStartAudioRecording -> startRecording()
            ChatEvents.OnStopAudioRecording -> stopRecording()
            ChatEvents.OnCancelAudioRecording -> stopRecording(true)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createNewChat() {
        val now = Clock.System.now()
        val formatted = now.toLocalDateTime(TimeZone.currentSystemDefault()).run {
            "${month.name.take(3)} $dayOfMonth, ${
                hour.toString().padStart(2, '0')
            }:${minute.toString().padStart(2, '0')}"
        }

        val newChat = Chat(
            id = Uuid.random().toString(),
            title = "New Chat $formatted",
            messages = emptyList(),
            createdAt = now.toEpochMilliseconds()
        )

        _state.update {
            it.copy(selectedChat = ChatState(chat = newChat))
        }
        loadTrendingTopics()
    }

    private fun loadChat(chatId: String) {
        viewModelScope.launch {
            _state.update { it.copy(selectedChat = ChatState(isChatLoading = true)) }
            chatDatabase.getChatById(chatId)
                .onSuccess { chat ->
                    _state.update {
                        it.copy(
                            selectedChat = it.selectedChat?.copy(
                                chat = chat,
                                isChatLoading = false
                            ),
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            selectedChat = it.selectedChat?.copy(isChatLoading = false)
                        )
                    }
                    mainEventBus.send(MainEvent.DatabaseError(error))
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun sendMessage(
        content: String,
        editedMessageId: String? = null,
        isTrending: Boolean = false
    ) {
        if (content.isBlank()) return
        if (content.length > MAX_MESSAGE_LENGTH) {
            _state.update {
                it.copy(
                    selectedChat = it.selectedChat?.copy(
                        messageLengthError = UiText.StringResourceId(Res.string.message_too_long)
                    )
                )
            }
            return
        }

        // Check if the message already exists and remove it if it does as we are editing
        editedMessageId?.let {
            checkIfMessageExistsAndRemoveIt(it)
        }

        val userMessage = Message(
            id = Uuid.random().toString(),
            content = content.trim(),
            image = null,
            role = Role.USER,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )

        _state.update {
            it.copy(
                selectedChat = it.selectedChat?.copy(
                    chat = it.selectedChat.chat.copy(messages = it.selectedChat.chat.messages + userMessage),
                    currentInput = "",
                    messageLengthError = null,
                    imageCompressionError = null
                )
            )
        }

        viewModelScope.launch {
            if (state.value.selectedChat?.shouldGenerateImage == true) {
                generateImage(content)
            } else {
                generateChatText(userMessage, isTrending)
            }
        }
    }

    private fun checkIfMessageExistsAndRemoveIt(id: String) {
        val currentMessages = _state.value.selectedChat?.chat?.messages?.toMutableList() ?: return
        val existingUserMessage = currentMessages.find { it.role == Role.USER && it.id == id }

        if (existingUserMessage != null) {
            val userMessageIndex = currentMessages.indexOf(existingUserMessage)

            // Remove user message
            currentMessages.removeAt(userMessageIndex)

            // After removal, the AI response (if it exists) will now be at the same index
            if (userMessageIndex < currentMessages.size &&
                currentMessages[userMessageIndex].role == Role.ASSISTANT
            ) {
                currentMessages.removeAt(userMessageIndex)  // Use same index!
            }

            _state.update {
                it.copy(
                    selectedChat = it.selectedChat?.copy(
                        chat = it.selectedChat.chat.copy(messages = currentMessages)
                    )
                )
            }
        }
    }

    private fun loadTrendingTopics() {
        viewModelScope.launch {
            _state.update { it.copy(selectedChat = it.selectedChat?.copy(isTrendingLoading = true)) }

            aiDataSource.getTrendingSearches()
                .onSuccess { topics ->
                    if (topics.isEmpty()) {
                        _state.update {
                            it.copy(
                                selectedChat = it.selectedChat?.copy(
                                    trendingTopics = listOf(
                                        "Latest physics discoveries",
                                        "Quantum mechanics explained",
                                        "Space exploration updates",
                                        "Cutting-edge science news",
                                        "Physics problem-solving tips"
                                    ),
                                    isTrendingLoading = false
                                )
                            )
                        }
                        return@onSuccess
                    }
                    _state.update {
                        it.copy(
                            selectedChat = it.selectedChat?.copy(
                                trendingTopics = topics,
                                isTrendingLoading = false
                            )
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(selectedChat = it.selectedChat?.copy(isTrendingLoading = false)) }
                    mainEventBus.send(MainEvent.NetworkingError(error))
                    log.e(tag = TAG, msg = { "Error loading trending topics: $error" })
                }
        }
    }

    private fun saveChat() {
        val chat = _state.value.selectedChat?.chat!!

        viewModelScope.launch {
            chatDatabase.saveChat(chat)
                .onSuccess {
                    log.i(tag = TAG, msg = { "Chat saved successfully" })
                    getAllChats()
                }
                .onError { error ->
                    log.e(tag = TAG, msg = { "Error saving chat: $error" })
                    mainEventBus.send(MainEvent.DatabaseError(error))
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateChatText(userMessage: Message, isTrending: Boolean = false) {
        _state.update { it.copy(selectedChat = it.selectedChat?.copy(isTyping = true)) }

        val responseBuilder = StringBuilder()
        val currentMessages = _state.value.selectedChat?.chat?.messages!!
        val prefs =
            if (isTrending) _state.value.selectedChat?.chatPreference?.copy(isOnlineSearchEnabled = true)!!
            else _state.value.selectedChat?.chatPreference!!

        // Create a temporary streaming message ID once
        val streamingMessageId = Uuid.random().toString()

        viewModelScope.launch {
            aiDataSource.chatWithAi(currentMessages, prefs)
                .onSuccess { flow ->
                    flow.collect { response ->
                        if (response.isNotEmpty()) {
                            responseBuilder.append(response)
                            _state.update { currentState ->
                                currentState.copy(
                                    selectedChat = currentState.selectedChat?.copy(
                                        chat = currentState.selectedChat.chat.copy(
                                            // Filter out previous streaming message and add updated one
                                            messages = currentState.selectedChat.chat.messages
                                                .filter { it.id != streamingMessageId } +
                                                    Message(
                                                        id = streamingMessageId,
                                                        content = responseBuilder.toString(),
                                                        image = null,
                                                        role = Role.ASSISTANT,
                                                        timestamp = Clock.System.now()
                                                            .toEpochMilliseconds()
                                                    )
                                        ),
                                        isTyping = true
                                    )
                                )
                            }
                        }
                    }

                    // Flow collection is complete, update with final message
                    _state.update { currentState ->
                        currentState.copy(
                            selectedChat = currentState.selectedChat?.copy(
                                chat = currentState.selectedChat.chat.copy(
                                    messages = currentState.selectedChat.chat.messages
                                        .filter { it.id != streamingMessageId } +
                                            Message(
                                                id = streamingMessageId,
                                                content = responseBuilder.toString(),
                                                image = null,
                                                role = Role.ASSISTANT,
                                                timestamp = Clock.System.now().toEpochMilliseconds()
                                            )
                                ),
                                isTyping = false
                            )
                        )
                    }
                    saveChat()
                    // If this is a new chat with 2 messages (user + AI), generate title
                    val shouldGenerateTitle =
                        _state.value.selectedChat?.chat?.messages?.count { it.role == Role.USER || it.role == Role.ASSISTANT } == 2
                                && _state.value.selectedChat?.chat?.title?.startsWith("New Chat") == true
                    if (shouldGenerateTitle) generateChatTitle(userMessage.content)
                }.onError { error ->
                    _state.update { it.copy(selectedChat = it.selectedChat?.copy(isTyping = false)) }
                    editMessage(_state.value.selectedChat?.chat?.messages?.last()?.id!!)
                    checkIfMessageExistsAndRemoveIt(_state.value.selectedChat?.chat?.messages?.last()?.id!!)
                    mainEventBus.send(MainEvent.NetworkingError(error))
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateImage(prompt: String) {
        _state.update { it.copy(selectedChat = it.selectedChat?.copy(isTyping = true)) }

        viewModelScope.launch {
            aiDataSource.generateImage(prompt)
                .onSuccess { images ->
                    val messages = images.map { image ->
                        Message(
                            id = Uuid.random().toString(),
                            content = "",
                            image = image,
                            role = Role.ASSISTANT,
                            timestamp = Clock.System.now().toEpochMilliseconds()
                        )
                    }.ifEmpty {
                        listOf(
                            Message(
                                id = Uuid.random().toString(),
                                content = "Image generation failed. Please try again later",
                                image = null,
                                role = Role.ASSISTANT,
                                timestamp = Clock.System.now().toEpochMilliseconds()
                            )
                        )
                    }
                    _state.update {
                        it.copy(
                            selectedChat = it.selectedChat?.copy(
                                chat = it.selectedChat.chat.copy(messages = it.selectedChat.chat.messages + messages),
                                isTyping = false
                            )
                        )
                    }
                    saveChat()
                }.onError { error ->
                    _state.update { it.copy(selectedChat = it.selectedChat?.copy(isTyping = false)) }
                    editMessage(_state.value.selectedChat?.chat?.messages?.last()?.id!!)
                    checkIfMessageExistsAndRemoveIt(_state.value.selectedChat?.chat?.messages?.last()?.id!!)
                    mainEventBus.send(MainEvent.NetworkingError(error))
                }
        }
    }

    private fun transcribeAudio(audioPath: String) {
        _state.update { it.copy(selectedChat = it.selectedChat?.copy(isTranscriptionLoading = true)) }

        viewModelScope.launch {
            aiDataSource.transcribeAudio(audioPath)
                .onSuccess { transcription ->
                    if (transcription.isBlank()) {
                        log.e(tag = TAG, msg = { "Transcription is empty" })
                        _state.update {
                            it.copy(
                                selectedChat = it.selectedChat?.copy(
                                    isAudioRecording = false,
                                    isTranscriptionLoading = false
                                )
                            )
                        }
                        return@onSuccess
                    }
                    _state.update {
                        it.copy(
                            selectedChat = it.selectedChat?.copy(
                                currentInput = transcription.trim(),
                                isAudioRecording = false,
                                isTranscriptionLoading = false
                            )
                        )
                    }
                }.onError { error ->
                    _state.update {
                        it.copy(
                            selectedChat = it.selectedChat?.copy(
                                isAudioRecording = false,
                                isTranscriptionLoading = false
                            )
                        )
                    }
                    mainEventBus.send(MainEvent.NetworkingError(error))
                }
        }
    }

    private fun editMessage(messageId: String) {
        val messageToEdit = _state.value.selectedChat?.chat?.messages?.find { it.id == messageId }
        messageToEdit?.let { message ->
            _state.update {
                it.copy(
                    selectedChat = it.selectedChat?.copy(
                        currentInput = message.content,
                    )
                )
            }
        }
    }

    private fun getAllChats() {
        viewModelScope.launch {
            _state.update { it.copy(isChatListLoading = true) }
            chatDatabase.getAllChats()
                .onSuccess { chats ->
                    _state.update { it.copy(chats = chats, isChatListLoading = false) }
                }
                .onError { error ->
                    _state.update { it.copy(isChatListLoading = false) }
                    mainEventBus.send(MainEvent.DatabaseError(error))
                }
        }
    }

    private fun deleteChat(chatId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isChatDeleting = true) }
            val shouldMakeSelectedChatNull = _state.value.selectedChat?.chat?.id == chatId
            chatDatabase.deleteChat(chatId)
                .onSuccess {
                    _state.update { it.copy(isChatDeleting = false) }
                    if (shouldMakeSelectedChatNull) {
                        _state.update { it.copy(selectedChat = null) }
                    }
                    getAllChats()
                }
                .onError { error ->
                    _state.update { it.copy(isChatDeleting = false) }
                    mainEventBus.send(MainEvent.DatabaseError(error))
                }
        }
    }

    private fun generateChatTitle(userMessage: String) {
        viewModelScope.launch {
            aiDataSource.generateChatTitle(userMessage)
                .onSuccess { title ->
                    if (title.isBlank()) return@onSuccess
                    _state.update {
                        it.copy(
                            selectedChat = it.selectedChat?.copy(
                                chat = it.selectedChat.chat.copy(
                                    title = title
                                )
                            )
                        )
                    }
                    saveChat()
                }
                .onError { error ->
                    mainEventBus.send(MainEvent.NetworkingError(error))
                    log.e(tag = TAG, msg = { "Error generating chat title: $error" })
                }
        }
    }

    private fun startRecording() {
        viewModelScope.launch {
            fileManager.createFile("prompt-audio.wav") {
                audioRecorder.start(it)
            }
            _state.update {
                it.copy(
                    selectedChat = it.selectedChat?.copy(
                        isAudioRecording = true,
                        audioPath = fileManager.getAbsolutePath("prompt-audio.wav")
                    )
                )
            }
        }
    }

    private fun stopRecording(isCancelling: Boolean = false) {
        viewModelScope.launch {
            if (isCancelling) {
                audioRecorder.stop()
                _state.update {
                    it.copy(
                        selectedChat = it.selectedChat?.copy(isAudioRecording = false)
                    )
                }
            } else {
                audioRecorder.stop()
                _state.value.selectedChat?.audioPath?.let {
                    transcribeAudio(it)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ChatViewModel"
        private const val MAX_IMAGE_SIZE = 1024 * 1024L // 1MB
        private const val MAX_MESSAGE_LENGTH = 4096
        val log = logging()
    }
}