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
    suspend fun upsertItems(items: List<GeomacItem>)

    @Upsert
    suspend fun upsertCoordinates(coordinates: List<GeomacCoordinates>)

    @Query("DELETE FROM GeomacItem WHERE mac = :mac")
    suspend fun delete(mac: Long)

    @Transaction
    suspend fun upsert(items: List<GeomacItemWithCoordinates>) {
        upsertItems(items.map { item -> GeomacItem(item.mac) })
        upsertCoordinates(items.flatMap { item -> item.coordinates })
    }

    @Transaction
    @Query("SELECT * FROM GeomacItem ORDER BY mac ASC")
    suspend fun getAll(): List<GeomacItemWithCoordinates>

    @Transaction
    @RawQuery(observedEntities = [GeomacItem::class, GeomacCoordinates::class])
    fun getPage(query: RoomRawQuery): PagingSource<Int, GeomacItemWithCoordinates>
}
