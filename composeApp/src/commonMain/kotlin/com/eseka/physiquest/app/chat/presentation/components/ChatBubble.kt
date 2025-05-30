package com.eseka.physiquest.app.chat.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eseka.physiquest.app.chat.domain.models.Message
import com.eseka.physiquest.app.chat.domain.models.Role
import com.eseka.physiquest.core.presentation.SaveImageToGalleryHandler

@Composable
fun ChatBubble(message: Message, onEditClicked: (String) -> Unit) {
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
                AsyncImage(
                    model = message.image,
                    contentDescription = null,
                    modifier = Modifier
                        .wrapContentHeight()
                        .clip(MaterialTheme.shapes.medium)
                        .padding(top = 4.dp),
                    contentScale = ContentScale.Crop
                )
                if (message.role != Role.USER) {
                    SaveImageToGalleryHandler(
                        imageUri = message.image,
                        saveIcon = Icons.Default.SaveAlt,
                        modifier = Modifier
                            .align(alignment = Alignment.End)
                            .padding(bottom = 4.dp),
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
                        ClickableFormattedText(
                            text = message.content,
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}