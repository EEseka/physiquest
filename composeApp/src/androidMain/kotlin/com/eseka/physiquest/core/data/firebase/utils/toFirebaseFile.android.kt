package com.eseka.physiquest.core.data.firebase.utils

import androidx.core.net.toUri
import dev.gitlive.firebase.storage.File as FirebaseFile

actual fun String.toFirebaseFile(): FirebaseFile {
    val uri = this.toUri()
    return FirebaseFile(uri)
}