package com.eseka.physiquest.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.aallam.openai.client.OpenAI
import com.eseka.physiquest.BuildConfig
import com.eseka.physiquest.app.MainEventBus
import com.eseka.physiquest.app.chat.data.FirestoreChatRepository
import com.eseka.physiquest.app.chat.data.OpenAiRepository
import com.eseka.physiquest.app.chat.data.local.DatabaseFactory
import com.eseka.physiquest.app.chat.data.local.TrendingSearchDatabase
import com.eseka.physiquest.app.chat.domain.AiDataSource
import com.eseka.physiquest.app.chat.domain.ChatDatabase
import com.eseka.physiquest.app.chat.presentation.ChatViewModel
import com.eseka.physiquest.app.settings.data.UserRepoImpl
import com.eseka.physiquest.app.settings.domain.UserRepo
import com.eseka.physiquest.app.settings.presentation.SettingsViewModel
import com.eseka.physiquest.authentication.data.CheckFirstInstallDataSource
import com.eseka.physiquest.authentication.data.FirebaseAuthRepositoryImpl
import com.eseka.physiquest.authentication.domain.CheckFirstInstallUseCase
import com.eseka.physiquest.authentication.domain.UserAuthRepo
import com.eseka.physiquest.authentication.domain.validation.ValidateEmail
import com.eseka.physiquest.authentication.domain.validation.ValidatePassword
import com.eseka.physiquest.authentication.domain.validation.ValidateRepeatedPassword
import com.eseka.physiquest.authentication.domain.validation.ValidateSignInPassword
import com.eseka.physiquest.authentication.presentation.AuthEventBus
import com.eseka.physiquest.authentication.presentation.signin.SignInViewModel
import com.eseka.physiquest.authentication.presentation.signup.SignUpViewModel
import com.eseka.physiquest.authentication.presentation.welcome.WelcomeViewModel
import com.eseka.physiquest.core.data.firebase.FirebaseMediaStorage
import com.eseka.physiquest.core.domain.MediaStorage
import com.eseka.physiquest.core.domain.validation.ValidateDisplayName
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    // Define shared dependencies here
    single { Firebase.auth }
    single { Firebase.storage }
    single { Firebase.firestore }

    singleOf(::FirebaseAuthRepositoryImpl).bind<UserAuthRepo>()
    singleOf(::FirebaseMediaStorage).bind<MediaStorage>()
    singleOf(::CheckFirstInstallDataSource).bind<CheckFirstInstallUseCase>()
    singleOf(::UserRepoImpl).bind<UserRepo>()
    singleOf(::OpenAiRepository).bind<AiDataSource>()
    singleOf(::FirestoreChatRepository).bind<ChatDatabase>()
    single {
        get<DatabaseFactory>().create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<TrendingSearchDatabase>().dao }
    single { OpenAI(BuildConfig.OPENAI_API_KEY) }

    single { ValidateEmail() }
    single { ValidatePassword() }
    single { ValidateRepeatedPassword() }
    single { ValidateSignInPassword() }
    single { ValidateDisplayName() }
    single { AuthEventBus() }
    single { MainEventBus() }

    viewModelOf(::SignUpViewModel)
    viewModelOf(::SignInViewModel)
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ChatViewModel)
}