package com.eseka.physiquest.app.chat.data

import com.diamondedge.logging.logging
import com.eseka.physiquest.app.chat.data.mappers.toChatSummary
import com.eseka.physiquest.app.chat.domain.ChatDatabase
import com.eseka.physiquest.app.chat.domain.models.Chat
import com.eseka.physiquest.app.chat.domain.models.ChatSummary
import com.eseka.physiquest.app.chat.domain.models.Message
import com.eseka.physiquest.app.chat.domain.models.Role
import com.eseka.physiquest.core.data.firebase.utils.safeFirebaseFirestoreCall
import com.eseka.physiquest.core.data.firebase.utils.safeFirebaseStorageCall
import com.eseka.physiquest.core.domain.MediaStorage
import com.eseka.physiquest.core.domain.utils.FirebaseFirestoreError
import com.eseka.physiquest.core.domain.utils.FirebaseStorageError
import com.eseka.physiquest.core.domain.utils.Result
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore,
    private val storage: MediaStorage,
    private val auth: FirebaseAuth
) : ChatDatabase {
    private val _currentUserFlow: StateFlow<FirebaseUser?> = callbackFlow {
        // Listen to auth state changes reactively
        val authStateJob = launch {
            auth.authStateChanged.collect { user ->
                trySend(user)
            }
        }

        // Periodically reload the current user to detect remote deletion
        val reloadJob = launch(Dispatchers.Default) {
            while (currentCoroutineContext().isActive) {
                delay(30_000)
                val user = auth.currentUser
                if (user != null) {
                    try {
                        user.reload()
                    } catch (e: Exception) {
                        log.e(
                            tag = TAG,
                            msg = { "Failed to reload user: ${e.message}" },
                            err = e
                        )
                        trySend(null)
                    }
                }
            }
        }

        awaitClose {
            authStateJob.cancel()
            reloadJob.cancel()
        }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        started = SharingStarted.Eagerly,
        initialValue = auth.currentUser
    )

    private val currentUserId: String?
        get() = _currentUserFlow.value?.uid

    override val user: FirebaseUser?
        get() = _currentUserFlow.value

    override suspend fun saveChat(chat: Chat): Result<Unit, FirebaseFirestoreError> {
        if (currentUserId == null) {
            return Result.Error(FirebaseFirestoreError.UNAUTHENTICATED)
        }

        val chatSummary = chat.toChatSummary(updatedAt = Clock.System.now().toEpochMilliseconds())
        return safeFirebaseFirestoreCall {
            val chatData = mapOf(
                "id" to chat.id,
                "userId" to currentUserId!!,
                "title" to chat.title,
                "createdAt" to chat.createdAt,
                "messages" to chat.messages.map { message ->
                    mapOf(
                        "id" to message.id,
                        "content" to message.content,
                        "image" to getSecureImageUrl(
                            chat.id,
                            message.id,
                            message.image
                        ),
                        "role" to message.role.name,
                        "timestamp" to message.timestamp
                    )
                },
            )
            val chatSummaryData = mapOf(
                "id" to chatSummary.id,
                "userId" to currentUserId!!,
                "title" to chatSummary.title,
                "createdAt" to chatSummary.createdAt,
                "updatedAt" to chatSummary.updatedAt,
            )

            firestore.collection("users")
                .document(currentUserId!!)
                .collection("chats")
                .document(chat.id)
                .set(chatData)
            firestore.collection("users")
                .document(currentUserId!!)
                .collection("chatSummaries")
                .document(chatSummary.id)
                .set(chatSummaryData)
        }
    }

    override suspend fun getChatById(chatId: String): Result<Chat, FirebaseFirestoreError> {
        if (currentUserId == null) {
            return Result.Error(FirebaseFirestoreError.UNAUTHENTICATED)
        }

        val document = firestore.collection("users")
            .document(currentUserId!!)
            .collection("chats")
            .document(chatId)
            .get()


        if (!document.exists) {
            return Result.Error(FirebaseFirestoreError.NOT_FOUND)
        }

        return safeFirebaseFirestoreCall {
            val id = document.get<String?>("id") ?: ""
            val title = document.get<String?>("title") ?: ""
            val createdAt = document.get<Long?>("createdAt") ?: 0L

            // Use the serializable data class
            val messagesData = document.get<List<MessageData>?>("messages") ?: emptyList()
            val messages = messagesData.map { messageData ->
                val role = when (messageData.role) {
                    "SYSTEM" -> Role.SYSTEM
                    "USER" -> Role.USER
                    "ASSISTANT" -> Role.ASSISTANT
                    else -> Role.USER
                }

                var image = messageData.image

                // Try to get the image from storage as it might not be saved properly in Firestore
                if (image == null) {
                    val path =
                        "${CHAT_IMAGE_STORAGE_PATH_START}${currentUserId!!}/$chatId${CHAT_IMAGE_STORAGE_PATH_END}/${messageData.id}"
                    storage.getPicture(path)
                        .onSuccess { secureUrl ->
                            image = secureUrl
                        }.onError { error ->
                            log.e(
                                tag = TAG,
                                msg = { "Failed to get image from Firebase Storage: $error" })
                        }
                }

                Message(
                    id = messageData.id,
                    role = role,
                    content = messageData.content,
                    image = image,
                    timestamp = messageData.timestamp
                )
            }

            Chat(id = id, title = title, messages = messages, createdAt = createdAt)
        }
    }


    override suspend fun getAllChats(): Result<List<ChatSummary>, FirebaseFirestoreError> {
        if (currentUserId == null) {
            return Result.Error(FirebaseFirestoreError.UNAUTHENTICATED)
        }

        return safeFirebaseFirestoreCall {
            val querySnapshot = firestore.collection("users")
                .document(currentUserId!!)
                .collection("chatSummaries")
                .orderBy("updatedAt", Direction.DESCENDING)
                .get()

            val chats = querySnapshot.documents.map { document ->
                val id = document.get<String?>("id") ?: ""
                val title = document.get<String?>("title") ?: ""
                val createdAt = document.get<Long?>("createdAt") ?: 0L
                val updatedAt = document.get<Long?>("updatedAt") ?: 0L
                ChatSummary(
                    id = id,
                    title = title,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            }
            chats
        }

    }

    override suspend fun deleteChat(chatId: String): Result<Unit, FirebaseFirestoreError> {
        if (currentUserId == null) {
            return Result.Error(FirebaseFirestoreError.UNAUTHENTICATED)
        }

        safeFirebaseStorageCall {
            val chatImagesPath =
                "${CHAT_IMAGE_STORAGE_PATH_START}${currentUserId!!}/$chatId${CHAT_IMAGE_STORAGE_PATH_END}"
            storage.deleteAllPictures(chatImagesPath)
                .onSuccess {
                    log.i(
                        tag = TAG,
                        msg = { "Successfully deleted all images for chat: $chatId" })
                }
                .onError { error ->
                    log.e(
                        tag = TAG,
                        msg = { "Error deleting images for chat $chatId: $error" })
                    return@safeFirebaseStorageCall Result.Error(FirebaseStorageError.IO_ERROR)
                }
        }

        return safeFirebaseFirestoreCall {
            firestore.collection("users")
                .document(currentUserId!!)
                .collection("chats")
                .document(chatId)
                .delete()
            firestore.collection("users")
                .document(currentUserId!!)
                .collection("chatSummaries")
                .document(chatId)
                .delete()
        }
    }

    private suspend fun getSecureImageUrl(
        chatId: String,
        messageId: String,
        image: String?
    ): String? {
        var finalImageUrl: String? = null
        image?.let { imageUri ->
            safeFirebaseStorageCall {
                val path =
                    "${CHAT_IMAGE_STORAGE_PATH_START}${currentUserId!!}/$chatId${CHAT_IMAGE_STORAGE_PATH_END}/${messageId}"
                storage.uploadPicture(path, imageUri)
                    .onSuccess { secureUrl ->
                        finalImageUrl = secureUrl
                    }.onError { error ->
                        log.e(
                            tag = TAG,
                            msg = { "Failed to upload image to Firebase Storage: $error" })
                        Result.Error(FirebaseStorageError.IO_ERROR)
                    }
            }
        }
        return finalImageUrl
    }

    companion object {
        private const val TAG = "FirestoreChatRepository"
        private const val CHAT_IMAGE_STORAGE_PATH_START = "chats/"
        private const val CHAT_IMAGE_STORAGE_PATH_END = "/images"
        val log = logging()
    }
}

@Serializable
data class MessageData(
    val id: String = "",
    val content: String = "",
    val image: String? = null,
    val role: String = "",
    val timestamp: Long = 0L
)