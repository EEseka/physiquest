package com.eseka.physiquest.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eseka.physiquest.app.navigation.MainNavigation
import com.eseka.physiquest.authentication.navigation.AuthNavigation
import com.eseka.physiquest.authentication.presentation.welcome.SplashScreen
import com.eseka.physiquest.authentication.presentation.welcome.WelcomeUiState
import com.eseka.physiquest.authentication.presentation.welcome.WelcomeViewModel
import com.eseka.physiquest.core.navigation.util.navigateAndClear
import org.koin.compose.viewmodel.koinViewModel

private const val TAG = "RootNavigation"

@Composable
fun RootNavigation(
    navController: NavHostController = rememberNavController(),
    welcomeViewModel: WelcomeViewModel = koinViewModel()
) {
    val welcomeState by welcomeViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = RootNavDestinations.Splash.route
    ) {
        composable(RootNavDestinations.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    when (welcomeState) {
                        WelcomeUiState.Authenticated -> {
                            navigateAndClear(navController, RootNavDestinations.Main.route)
                        }

                        else -> {
                            navigateAndClear(navController, RootNavDestinations.Auth.route)
                        }
                    }
                }
            )
        }

        composable(RootNavDestinations.Auth.route) {
            AuthNavigation(
                welcomeState = welcomeState,
                welcomeViewModel = welcomeViewModel,
                onNavigateToHome = {
                    navigateAndClear(navController, RootNavDestinations.Main.route)
                }
            )
        }

        composable(RootNavDestinations.Main.route) {
            MainNavigation(
                onLogout = {
                    navigateAndClear(navController, RootNavDestinations.Auth.route)
                }
            )
        }
    }
}