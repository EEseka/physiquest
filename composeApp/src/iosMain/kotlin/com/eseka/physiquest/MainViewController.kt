package com.eseka.physiquest

import androidx.compose.ui.window.ComposeUIViewController
import com.eseka.physiquest.di.initKoin
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
        GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = "468943217223-iinjom88bhgdqco4dceldrunlps1sfq2.apps.googleusercontent.com"))
    }
) { App() }