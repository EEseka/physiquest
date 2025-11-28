package com.eseka.physiquest.app.physics.data.network

import com.eseka.physiquest.BuildConfig
import com.eseka.physiquest.app.physics.data.dto.AstronomyPictureOfTheDayDto
import com.eseka.physiquest.app.physics.data.mappers.toAstronomyPictureOfTheDay
import com.eseka.physiquest.app.physics.domain.AstronomyRepository
import com.eseka.physiquest.app.physics.domain.models.nasa.AstronomyPictureOfTheDay
import com.eseka.physiquest.core.data.networking.constructUrl
import com.eseka.physiquest.core.data.networking.safeCall
import com.eseka.physiquest.core.domain.utils.DataError
import com.eseka.physiquest.core.domain.utils.Result
import com.eseka.physiquest.core.domain.utils.map
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class RemoteAstronomyDataSource(private val httpClient: HttpClient) : AstronomyRepository {
    override suspend fun getAstronomyPictureOfTheDay(): Result<AstronomyPictureOfTheDay, DataError.Remote> =
        safeCall<AstronomyPictureOfTheDayDto> {
            httpClient.get(urlString = constructUrl("planetary/apod?api_key=${BuildConfig.NASA_API_KEY}"))
        }.map { response ->
            response.toAstronomyPictureOfTheDay()
        }
}