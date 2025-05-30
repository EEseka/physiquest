package com.eseka.physiquest.app.chat.data.local.mappers

import com.eseka.physiquest.app.chat.data.local.entities.TrendingSearchEntity
import kotlinx.datetime.Clock

fun List<String>.toTrendingSearchEntity(): TrendingSearchEntity {
    return TrendingSearchEntity(
        trendingTopics = this,
        timestamp = Clock.System.now().toEpochMilliseconds()
    )
}

fun TrendingSearchEntity.toTrendingSearch(): List<String> {
    return this.trendingTopics
}