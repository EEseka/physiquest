package com.eseka.physiquest.core.domain.services

interface AudioPermissionHandler {
    suspend fun requestPermission(): Boolean
    suspend fun hasPermission(): Boolean
}

