package com.eseka.physiquest.core.data.firebase.utils

import platform.Foundation.NSURL
import dev.gitlive.firebase.storage.File as FirebaseFile

actual fun String.toFirebaseFile(): FirebaseFile {
    val cleanPath = this.replace("file://", "")
    return FirebaseFile(NSURL.fileURLWithPath(cleanPath))
}