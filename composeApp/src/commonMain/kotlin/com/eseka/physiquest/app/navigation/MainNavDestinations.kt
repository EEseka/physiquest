package com.eseka.physiquest.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.eseka.physiquest.core.presentation.UiText
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.chat
import physiquest.composeapp.generated.resources.settings

sealed class MainNavDestinations(
    val route: String,
    val label: UiText,
    val outlinedIcon: ImageVector? = null,
    val filledIcon: ImageVector? = null
) {
    data object Chat : MainNavDestinations(
        "chat",
        UiText.StringResourceId(Res.string.chat),
        Icons.AutoMirrored.Outlined.Chat,
        Icons.AutoMirrored.Filled.Chat
    )

    data object ChatDetail : MainNavDestinations(
        "chat_detail",
        UiText.StringResourceId(Res.string.chat),
        Icons.AutoMirrored.Outlined.Chat,
        Icons.AutoMirrored.Filled.Chat
    )

    data object Settings : MainNavDestinations(
        "settings",
        UiText.StringResourceId(Res.string.settings),
        Icons.Outlined.Settings,
        Icons.Filled.Settings
    )
}