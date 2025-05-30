package com.eseka.physiquest.core.navigation.util

import androidx.navigation.NavHostController

fun navigateAndClear(navHostController: NavHostController, route: String) {
    navHostController.navigate(route) {
        popUpTo(0) { inclusive = true }
    }
}