package com.eseka.physiquest.core.data.services

import com.eseka.physiquest.core.domain.services.AudioPermissionHandler

expect class AudioPermissionHandlerImpl : AudioPermissionHandler {
    override suspend fun requestPermission(): Boolean
    override suspend fun hasPermission(): Boolean
}