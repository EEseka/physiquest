package com.eseka.physiquest.app.chat.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eseka.physiquest.app.chat.domain.models.Message
import com.eseka.physiquest.app.chat.domain.models.Role
import com.eseka.physiquest.core.presentation.SaveImageToGalleryHandler
import com.eseka.physiquest.core.presentation.components.shimmerEffect

@Composable
fun ChatBubble(
    message: Message,
    onEditClicked: (String) -> Unit,
    onImageSaveComplete: () -> Unit,
    onImageSaveError: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = if (message.role == Role.USER) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (message.role == Role.USER) Alignment.End else Alignment.Start
        ) {
            if (!message.image.isNullOrEmpty()) {
                var isImageLoading by remember { mutableStateOf(true) }

                Box(modifier = Modifier.wrapContentHeight()) {
                    AsyncImage(
                        model = message.image,
                        contentDescription = null,
                        modifier = Modifier
                            .wrapContentHeight()
                            .clip(MaterialTheme.shapes.medium)
                            .padding(top = 4.dp),
                        contentScale = ContentScale.Crop,
                        onLoading = { isImageLoading = true },
                        onSuccess = { isImageLoading = false }
                    )

                    if (isImageLoading) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .shimmerEffect()
                        )
                    }
                }
                if (!isImageLoading && message.role != Role.USER) {
                    SaveImageToGalleryHandler(
                        imageUri = message.image,
                        saveIcon = Icons.Default.SaveAlt,
                        onSaveComplete = onImageSaveComplete,
                        onError = onImageSaveError,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(bottom = 4.dp)
                    )
                }
            }
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (message.role == Role.USER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            ) {
                if (message.role == Role.USER) {
                    UserMessageText(
                        text = message.content,
                        onEditClick = { onEditClicked(message.id) }
                    )
                } else {
                    SelectionContainer {
                        FormattedMessage(
                            content = message.content,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            linkColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}