package com.eseka.physiquest.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eseka.physiquest.app.MainEvent
import com.eseka.physiquest.app.MainEventBus
import com.eseka.physiquest.app.chat.presentation.ChatDetailScreen
import com.eseka.physiquest.app.chat.presentation.ChatEvents
import com.eseka.physiquest.app.chat.presentation.ChatListScreen
import com.eseka.physiquest.app.chat.presentation.ChatViewModel
import com.eseka.physiquest.app.settings.presentation.SettingsEvents
import com.eseka.physiquest.app.settings.presentation.SettingsScreen
import com.eseka.physiquest.app.settings.presentation.SettingsViewModel
import com.eseka.physiquest.app.settings.presentation.components.GoogleReAuthBottomSheet
import com.eseka.physiquest.app.settings.presentation.components.PasswordReAuthBottomSheet
import com.eseka.physiquest.core.presentation.UiText
import com.eseka.physiquest.core.presentation.utils.ObserveAsEvents
import com.eseka.physiquest.core.presentation.utils.toUiText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.chats
import physiquest.composeapp.generated.resources.new_chat
import physiquest.composeapp.generated.resources.profile_updated_successfully
import physiquest.composeapp.generated.resources.settings

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    mainEventBus: MainEventBus = koinInject(),
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showGoogleReAuthSheet by remember { mutableStateOf(false) }
    var showPasswordReAuthSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val profileUpdatedSuccessfullyString =
        UiText.StringResourceId(Res.string.profile_updated_successfully).asString()
    var errorToShow by remember { mutableStateOf<UiText?>(null) }

    errorToShow?.let { uiText ->
        val errorMessage = uiText.asString()
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            errorToShow = null
        }
    }

    ObserveAsEvents(events = mainEventBus.events) { event ->
        when (event) {
            is MainEvent.AuthError -> {
                errorToShow = event.error.toUiText()
            }

            is MainEvent.DatabaseError -> {
                errorToShow = event.error.toUiText()
            }

            is MainEvent.NetworkingError -> {
                errorToShow = event.error.toUiText()
            }

            MainEvent.ProfileUpdateComplete -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = profileUpdatedSuccessfullyString,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            MainEvent.AccountDeletionComplete -> {
                onLogout()
            }

            MainEvent.SignOutComplete -> {
                onLogout()
            }

            MainEvent.ReAuthenticateWithGoogle -> {
                showGoogleReAuthSheet = true
            }

            MainEvent.ReAuthenticateWithPassword -> {
                showPasswordReAuthSheet = true
            }
        }
    }

    val screens = listOf(MainNavDestinations.Chat, MainNavDestinations.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val chatViewModel = koinViewModel<ChatViewModel>()
    val chatState by chatViewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            getTopAppBar(currentDestination?.route)
        },
        bottomBar = {
            if (currentDestination?.route != MainNavDestinations.ChatDetail.route) {
                NavigationBar {
                    screens.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    navController.graph.findStartDestination().route?.let { startRoute ->
                                        popUpTo(startRoute) {
                                            saveState = true
                                        }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.filledIcon!! else screen.outlinedIcon!!,
                                    contentDescription = null
                                )
                            },
                            label = { Text(screen.label.asString()) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == MainNavDestinations.Chat.route) {
                FloatingActionButton(
                    onClick = {
                        chatViewModel.onEvent(ChatEvents.OnCreateNewChat)
                        navController.navigate(MainNavDestinations.ChatDetail.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.new_chat)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = MaterialTheme.shapes.large,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainNavDestinations.Chat.route
        ) {
            composable(MainNavDestinations.Chat.route) {
                ChatListScreen(
                    state = chatState,
                    onChatClicked = { chatId ->
                        chatViewModel.onEvent(ChatEvents.OnChatSelected(chatId))
                        navController.navigate(MainNavDestinations.ChatDetail.route)
                    },
                    onDeleteChatClicked = { chatId ->
                        chatViewModel.onEvent(ChatEvents.OnDeleteChat(chatId))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable(MainNavDestinations.ChatDetail.route) {
                ChatDetailScreen(
                    chatState = chatState.selectedChat,
                    onSaveTitleEdit = { chatViewModel.onEvent(ChatEvents.OnSaveTitleEdit) },
                    onTitleChanged = {
                        chatViewModel.onEvent(ChatEvents.OnChatTitleChanged(it))
                    },
                    onTrendingTopicSelected = {
                        chatViewModel.onEvent(ChatEvents.OnTrendingTopicSelected(it))
                    },
                    onInputChanged = {
                        chatViewModel.onEvent(ChatEvents.OnInputChanged(it))
                    },

                    onToggleReasoning = {
                        chatViewModel.onEvent(ChatEvents.OnToggleReasoning)
                    },
                    onToggleOnlineSearch = {
                        chatViewModel.onEvent(ChatEvents.OnToggleOnlineSearch)
                    },
                    onToggleImageGeneration = {
                        chatViewModel.onEvent(ChatEvents.OnToggleImageGeneration)
                    },
                    onSendMessage = {
                        chatViewModel.onEvent(ChatEvents.OnMessageSent)
                    },
                    onEditMessageClicked = {
                        chatViewModel.onEvent(ChatEvents.OnEditMessage(it))
                    },
                    onEditedMessageSent = {
                        chatViewModel.onEvent(ChatEvents.OnEditedMessageSent(it))
                    },
                    startAudioRecording = {
                        chatViewModel.onEvent(ChatEvents.OnStartAudioRecording)
                    },
                    stopAudioRecording = {
                        chatViewModel.onEvent(ChatEvents.OnStopAudioRecording)
                    },
                    onCancelAudioRecording = {
                        chatViewModel.onEvent(ChatEvents.OnCancelAudioRecording)
                    },
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
            composable(MainNavDestinations.Settings.route) {
                val settingsViewModel = koinViewModel<SettingsViewModel>()
                val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()

                SettingsScreen(
                    state = settingsState,
                    onDisplayNameChanged = {
                        settingsViewModel.onEvent(SettingsEvents.OnDisplayNameChanged(it))
                    },
                    onUpdateProfileClicked = {
                        settingsViewModel.onEvent(SettingsEvents.OnUpdateProfileClicked)
                    },
                    onDeleteAccountClicked = {
                        settingsViewModel.onEvent(SettingsEvents.OnDeleteAccountClicked)
                    },
                    onSignOutClicked = {
                        settingsViewModel.onEvent(SettingsEvents.OnSignOutClicked)
                    },
                    onPhotoSelected = { uri, extension ->
                        settingsViewModel.onEvent(
                            SettingsEvents.OnPhotoSelected(
                                uri,
                                extension
                            )
                        )
                    },
                    onScreenLeave = {
                        settingsViewModel.onEvent(SettingsEvents.OnScreenLeave)
                    },
                    onScreenReturn = {
                        settingsViewModel.onEvent(SettingsEvents.OnScreenReturn)
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                if (showGoogleReAuthSheet) {
                    GoogleReAuthBottomSheet(
                        onDismissRequest = { showGoogleReAuthSheet = false },
                        onGoogleResult = { idToken, accessToken ->
                            settingsViewModel.onEvent(
                                SettingsEvents.OnReAuthenticateWithGoogle(idToken, accessToken)
                            )
                            showGoogleReAuthSheet = false
                        }
                    )
                }

                if (showPasswordReAuthSheet) {
                    PasswordReAuthBottomSheet(
                        onDismissRequest = { showPasswordReAuthSheet = false },
                        onPasswordSubmit = { password ->
                            settingsViewModel.onEvent(
                                SettingsEvents.OnReAuthenticateWithPassword(password)
                            )
                            showPasswordReAuthSheet = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun getTopAppBar(screenRoute: String?) {
    screenRoute?.let { route ->
        when (route) {
            MainNavDestinations.Chat.route -> {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.chats)) },
                )
            }

            MainNavDestinations.ChatDetail.route -> {
                // Its handled in ChatDetailScreen
            }

            MainNavDestinations.Settings.route -> {
                TopAppBar(title = { Text(stringResource(Res.string.settings)) })
            }
        }
    }
}