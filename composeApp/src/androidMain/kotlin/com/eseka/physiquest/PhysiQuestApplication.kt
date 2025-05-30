package com.eseka.physiquest

import android.app.Application
import com.eseka.physiquest.di.initKoin
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.koin.android.ext.koin.androidContext

class PhysiQuestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@PhysiQuestApplication)
        }
        GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = "468943217223-iinjom88bhgdqco4dceldrunlps1sfq2.apps.googleusercontent.com"))
    }
}