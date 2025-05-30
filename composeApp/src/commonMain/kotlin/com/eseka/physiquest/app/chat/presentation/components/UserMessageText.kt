package com.eseka.physiquest.app.chat.presentation.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.chat.presentation.utils.SetClipboardText
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.copy
import physiquest.composeapp.generated.resources.edit

@Composable
fun UserMessageText(
    text: String,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit
) {
    var shouldCopyText by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box {
        Text(
            text = text,
            modifier = modifier
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showDropdownMenu = true }
                )
                .padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )

        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.copy)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null
                    )
                },
                onClick = {
                    shouldCopyText = true
                    showDropdownMenu = false
                }
            )
            if (shouldCopyText) {
                SetClipboardText(text)
                shouldCopyText = false
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.edit)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                },
                onClick = {
                    onEditClick()
                    showDropdownMenu = false
                }
            )
        }
    }
}