package com.eseka.physiquest.app.chat.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(
    private val context: Context
) {
    actual fun create(): RoomDatabase.Builder<TrendingSearchDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(TrendingSearchDatabase.DATABASE_NAME)

        return Room.databaseBuilder(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}