package com.eseka.physiquest.app.physics.domain

import com.eseka.physiquest.app.physics.domain.models.nasa.AstronomyPictureOfTheDay
import com.eseka.physiquest.core.domain.utils.DataError
import com.eseka.physiquest.core.domain.utils.Result

interface AstronomyRepository {
    suspend fun getAstronomyPictureOfTheDay(): Result<AstronomyPictureOfTheDay, DataError.Remote>
}