package com.eseka.physiquest.core.domain.utils

import physiquest.composeapp.generated.resources.Res

suspend fun readResourceFile(fileName: String): String {
    val bytes = Res.readBytes("files/$fileName")
    return bytes.decodeToString()
}