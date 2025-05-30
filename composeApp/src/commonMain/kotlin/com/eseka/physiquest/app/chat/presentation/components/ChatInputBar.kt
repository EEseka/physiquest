package com.eseka.physiquest.app.chat.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.core.presentation.AudioRecordingPermissionHandler
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.ask_anything
import physiquest.composeapp.generated.resources.describe_image_gen
import physiquest.composeapp.generated.resources.editing
import physiquest.composeapp.generated.resources.generate_image
import physiquest.composeapp.generated.resources.reason
import physiquest.composeapp.generated.resources.recording
import physiquest.composeapp.generated.resources.search

@Composable
fun ChatInputBar(
    currentInput: String,
    isTyping: Boolean,
    isRecording: Boolean,
    isTranscribing: Boolean,
    error: String? = null,
    isEditingMessage: Boolean,
    isReasoningEnabled: Boolean,
    isOnlineSearchEnabled: Boolean,
    isGenerateImageEnabled: Boolean,
    onInputChanged: (String) -> Unit,
    onToggleReasoning: () -> Unit,
    onToggleOnlineSearch: () -> Unit,
    onToggleImageGeneration: () -> Unit,
    onStartAudioRecording: () -> Unit,
    onStopAudioRecording: () -> Unit,
    onCancelAudioRecording: () -> Unit,
    onSendMessage: () -> Unit,
    onSendEditedMessage: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showImageGenInputBar by rememberSaveable { mutableStateOf(false) }

    // Get focus manager for keyboard dismissal
    val focusManager = LocalFocusManager.current

    if (isRecording) {
        AudioBar(
            isTranscribing = isTranscribing,
            onStopRecording = onStopAudioRecording,
            onCancelRecording = onCancelAudioRecording,
            modifier = modifier.fillMaxWidth()
        )
    } else {
        if (showImageGenInputBar) {
            ImageGenInputBar(
                value = currentInput,
                focusManager = focusManager,
                isTyping = isTyping,
                error = error,
                isEditingMessage = isEditingMessage,
                onCancelEdit = onCancelEdit,
                onToggleImageGeneration = {
                    showImageGenInputBar = false
                    onToggleImageGeneration()
                },
                onValueChange = { onInputChanged(it) },
                onVoiceInput = onStartAudioRecording,
                onSendMessage = onSendMessage,
                onSendEditedMessage = onSendEditedMessage,
                modifier = modifier
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = isOnlineSearchEnabled,
                        onClick = {
                            onToggleOnlineSearch()
                        },
                        label = { Text(stringResource(Res.string.search)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = null,
                            )
                        }
                    )
                    FilterChip(
                        selected = isReasoningEnabled,
                        onClick = {
                            onToggleReasoning()
                        },
                        label = { Text(stringResource(Res.string.reason)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                            )
                        }
                    )
                    FilterChip(
                        selected = isGenerateImageEnabled,
                        onClick = {
                            showImageGenInputBar = !showImageGenInputBar
                            onToggleImageGeneration()
                        },
                        label = { Text(stringResource(Res.string.generate_image)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Brush,
                                contentDescription = null,
                            )
                        }
                    )
                }

                // Text Input
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    shape = MaterialTheme.shapes.medium,
                    supportingText = error?.let {
                        { Text(text = it, color = MaterialTheme.colorScheme.error) }
                    },
                    value = currentInput,
                    onValueChange = { onInputChanged(it) },
                    placeholder = { Text(stringResource(Res.string.ask_anything)) },
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (currentInput.isNotBlank()) ImeAction.Send else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (currentInput.isNotBlank() && !isTyping) {
                                if (isEditingMessage) {
                                    onSendEditedMessage()
                                } else {
                                    onSendMessage()
                                }
                                focusManager.clearFocus()
                            }
                        },
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    trailingIcon = {
                        Row {
                            AudioRecordingPermissionHandler(
                                micIcon = Icons.Default.Mic,
                                onStartRecording = {
                                    focusManager.clearFocus() // Dismiss keyboard before recording
                                    onStartAudioRecording()
                                }
                            )
                            val sendAction =
                                if (isEditingMessage) onSendEditedMessage else onSendMessage
                            IconButton(
                                onClick = {
                                    sendAction()
                                    focusManager.clearFocus() // Dismiss keyboard after sending
                                },
                                enabled = currentInput.isNotBlank() && !isTyping
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    label = if (isEditingMessage) {
                        {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(Res.string.editing))
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        onCancelEdit()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    } else null,
                )
            }
        }
    }
}

@Composable
fun AudioBar(
    isTranscribing: Boolean,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCancelRecording) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated recording indicator
            val infiniteTransition = rememberInfiniteTransition(label = "recording")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = alpha))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(Res.string.recording),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (isTranscribing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )
        } else {
            IconButton(onClick = onStopRecording) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ImageGenInputBar(
    value: String,
    focusManager: FocusManager,
    error: String?,
    isTyping: Boolean,
    isEditingMessage: Boolean,
    onCancelEdit: () -> Unit,
    onToggleImageGeneration: () -> Unit,
    onValueChange: (String) -> Unit,
    onVoiceInput: () -> Unit,
    onSendMessage: () -> Unit,
    onSendEditedMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilterChip(
            selected = true,
            onClick = onToggleImageGeneration,
            label = { Text(stringResource(Res.string.generate_image)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Brush,
                    contentDescription = null,
                )
            }
        )
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            shape = MaterialTheme.shapes.medium,
            value = value,
            supportingText = error?.let {
                { Text(text = it, color = MaterialTheme.colorScheme.error) }
            },
            onValueChange = { onValueChange(it) },
            placeholder = { Text(stringResource(Res.string.describe_image_gen)) },
            maxLines = 5,
            trailingIcon = {
                Row {
                    AudioRecordingPermissionHandler(
                        micIcon = Icons.Default.Mic,
                        onStartRecording = onVoiceInput
                    )
                    val sendAction = if (isEditingMessage) onSendEditedMessage else onSendMessage
                    IconButton(
                        onClick = sendAction,
                        enabled = value.isNotBlank() && !isTyping,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = if (value.isNotBlank()) ImeAction.Send else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (value.isNotBlank() && !isTyping) {
                        if (isEditingMessage) {
                            onSendEditedMessage()
                        } else {
                            onSendMessage()
                        }
                        focusManager.clearFocus()
                    }
                },
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            label = if (isEditingMessage) {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(Res.string.editing))
                        IconButton(onClick = onCancelEdit) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
                        }
                    }
                }
            } else null,
        )
    }
}