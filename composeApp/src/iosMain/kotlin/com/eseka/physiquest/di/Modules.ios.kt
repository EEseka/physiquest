package com.eseka.physiquest.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.eseka.physiquest.app.chat.data.local.DatabaseFactory
import com.eseka.physiquest.core.data.services.AudioRecorderImpl
import com.eseka.physiquest.core.data.services.FileManagerImpl
import com.eseka.physiquest.core.data.services.ImageCompressorImpl
import com.eseka.physiquest.core.domain.services.AudioRecorder
import com.eseka.physiquest.core.domain.services.FileManager
import com.eseka.physiquest.core.domain.services.ImageCompressor
import com.eseka.physiquest.di.utils.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<DataStore<Preferences>> {
            createDataStore()
        }
        single { DatabaseFactory() }
        single<FileManager> { FileManagerImpl() }
        single<ImageCompressor> { ImageCompressorImpl() }
        single<AudioRecorder> { AudioRecorderImpl() }
    }