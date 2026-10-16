package io.silentsea.geomac.domain.repositories

import androidx.paging.PagingData
import io.silentsea.geomac.domain.model.Geomac
import kotlinx.coroutines.flow.Flow
import kotlinx.io.RawSink
import kotlinx.io.RawSource

interface GeomacRepository {
    fun observe(ranges: List<LongRange>): Flow<PagingData<Geomac>>

    suspend fun search(mac: Long): Int

    suspend fun delete(mac: Long)

    suspend fun restore(item: Geomac)

    suspend fun export(sink: RawSink): Int

    suspend fun import(source: RawSource): Int
}
