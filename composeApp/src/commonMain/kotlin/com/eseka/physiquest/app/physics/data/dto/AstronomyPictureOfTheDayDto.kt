package com.eseka.physiquest.app.physics.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AstronomyPictureOfTheDayDto(
    val title: String,
    val date: String,
    val explanation: String,
    val url: String? = null,
    val copyright: String? = null,
    @SerialName("hdurl") val hdUrl: String? = null,
)
