package io.silentsea.geomac.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import io.silentsea.geomac.data.db.entities.GeomacCoordinates
import io.silentsea.geomac.data.db.entities.GeomacItem

@Database(
    entities = [
        GeomacItem::class,
        GeomacCoordinates::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun geomacDao(): GeomacDao
}