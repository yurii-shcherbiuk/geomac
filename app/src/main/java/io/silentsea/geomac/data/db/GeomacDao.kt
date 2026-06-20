package io.silentsea.geomac.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import androidx.room.Upsert
import io.silentsea.geomac.data.db.entities.GeomacCoordinates
import io.silentsea.geomac.data.db.entities.GeomacItem
import io.silentsea.geomac.data.db.entities.GeomacItemWithCoordinates

@Dao
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