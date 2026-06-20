package io.silentsea.geomac.domain.repositories

import androidx.paging.PagingSource
import io.silentsea.geomac.data.db.entities.GeomacItemWithCoordinates
import kotlinx.coroutines.flow.Flow

interface GeomacRepository {
    fun search(vararg macs: Long): Flow<Long>

    suspend fun undo(vararg items: GeomacItemWithCoordinates)

    suspend fun delete(vararg macs: Long)

    suspend fun getAll(): List<GeomacItemWithCoordinates>

    fun getAllInRanges(vararg ranges: Pair<Long, Long>): PagingSource<Int, GeomacItemWithCoordinates>
}