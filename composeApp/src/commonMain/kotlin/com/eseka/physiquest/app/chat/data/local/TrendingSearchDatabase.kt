package com.eseka.physiquest.app.chat.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eseka.physiquest.app.chat.data.local.entities.TrendingSearchEntity

@Database(
    entities = [TrendingSearchEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
@ConstructedBy(TrendingSearchDatabaseConstructor::class)
abstract class TrendingSearchDatabase : RoomDatabase() {
    abstract val dao: TrendingSearchDao

    companion object {
        const val DATABASE_NAME = "trending_searches_db"
    }
}