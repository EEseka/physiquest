package com.eseka.physiquest.app.chat.data.local

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<TrendingSearchDatabase>
}
