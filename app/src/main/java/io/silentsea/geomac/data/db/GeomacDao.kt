package io.silentsea.geomac.data.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.silentsea.geomac.data.db.entities.GeomacCoordinates
import io.silentsea.geomac.data.db.entities.GeomacItem
import io.silentsea.geomac.data.db.entities.GeomacItemWithCoordinates

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface GeomacDao {
    @Upsert
    suspend fun upsert(vararg items: GeomacItem)

    @Upsert
    suspend fun upsert(vararg items: GeomacCoordinates)

    @Query("DELETE FROM GeomacItem WHERE mac IN (:macs)")
    suspend fun delete(vararg macs: Long)

    @Transaction
    suspend fun undo(vararg items: GeomacItemWithCoordinates) = items.forEach { item ->
        upsert(GeomacItem(mac = item.mac))
        upsert(*item.coordinates.toTypedArray())
    }

    @Transaction
    @Query("SELECT * FROM GeomacItem")
    suspend fun getAll(): List<GeomacItemWithCoordinates>

    @Transaction
    @RawQuery(observedEntities = [GeomacItem::class, GeomacCoordinates::class])
    fun getAll(query: RoomRawQuery): PagingSource<Int, GeomacItemWithCoordinates>
}