package com.eseka.physiquest.app.physics.data.mappers

import com.eseka.physiquest.app.physics.data.dto.AstronomyPictureOfTheDayDto
import com.eseka.physiquest.app.physics.domain.models.nasa.AstronomyPictureOfTheDay

fun AstronomyPictureOfTheDayDto.toAstronomyPictureOfTheDay(): AstronomyPictureOfTheDay {
    return AstronomyPictureOfTheDay(
        copyright = copyright,
        date = date,
        explanation = explanation,
        hdUrl = hdUrl ?: url ?: "",
        title = title
    )
}