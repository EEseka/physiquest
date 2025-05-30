package com.eseka.physiquest.app.chat.data.local

import androidx.room.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object TrendingSearchDatabaseConstructor : RoomDatabaseConstructor<TrendingSearchDatabase> {
    override fun initialize(): TrendingSearchDatabase
}