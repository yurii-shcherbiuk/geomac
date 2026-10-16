package io.silentsea.geomac.data.repositories

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room3.RoomRawQuery
import io.silentsea.geomac.data.backup.BackupEntry
import io.silentsea.geomac.data.backup.BackupJson
import io.silentsea.geomac.data.backup.toBackup
import io.silentsea.geomac.data.backup.toDomain
import io.silentsea.geomac.data.db.GeomacDao
import io.silentsea.geomac.data.db.entities.GeomacCoordinates
import io.silentsea.geomac.data.db.entities.GeomacItem
import io.silentsea.geomac.data.db.toDomain
import io.silentsea.geomac.data.db.toEntity
import io.silentsea.geomac.data.remote.LocationProvider
import io.silentsea.geomac.domain.model.Geomac
import io.silentsea.geomac.domain.model.Mac
import io.silentsea.geomac.domain.model.macString
import io.silentsea.geomac.domain.repositories.GeomacRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.asInputStream
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

class GeomacRepositoryImpl(
    private val geomacDao: GeomacDao,
    private val providers: List<LocationProvider>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GeomacRepository {
    override fun observe(ranges: List<LongRange>): Flow<PagingData<Geomac>> =
        Pager(PagingConfig(pageSize = 20, enablePlaceholders = false)) {
            geomacDao.getPage(pageQuery(ranges))
        }.flow.map { page -> page.map { it.toDomain() } }

    override suspend fun search(mac: Long): Int = withContext(ioDispatcher) {
        require(mac in Mac.RANGE) { "Invalid MAC address: $mac" }

        geomacDao.upsertItems(listOf(GeomacItem(mac)))

        val found = coroutineScope {
            providers.map { provider ->
                async {
                    try {
                        provider.locate(mac)?.let { coordinates ->
                            GeomacCoordinates(
                                mac,
                                provider.service,
                                coordinates.latitude,
                                coordinates.longitude
                            )
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "${provider.service} failed for ${mac.macString()}", e)
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        geomacDao.upsertCoordinates(found)

        found.size
    }

    override suspend fun delete(mac: Long) = withContext(ioDispatcher) {
        geomacDao.delete(mac)
    }

    override suspend fun restore(item: Geomac) = withContext(ioDispatcher) {
        geomacDao.upsert(listOf(item.toEntity()))
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun export(sink: RawSink): Int = withContext(ioDispatcher) {
        val entries = geomacDao.getAll().map { it.toDomain().toBackup() }

        sink.buffered().use { buffered ->
            BackupJson.encodeToStream(
                ListSerializer(BackupEntry.serializer()),
                entries,
                buffered.asOutputStream()
            )
        }

        entries.size
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun import(source: RawSource): Int = withContext(ioDispatcher) {
        val entries = source.buffered().use { buffered ->
            BackupJson.decodeFromStream(
                ListSerializer(BackupEntry.serializer()),
                buffered.asInputStream()
            )
        }

        val items = entries.mapNotNull { it.toDomain()?.toEntity() }

        geomacDao.upsert(items)

        items.size
    }

    private fun pageQuery(ranges: List<LongRange>): RoomRawQuery {
        if (ranges.isEmpty()) return RoomRawQuery("SELECT * FROM GeomacItem ORDER BY mac ASC")

        val where = ranges.joinToString(" OR ") { "(mac BETWEEN ? AND ?)" }

        return RoomRawQuery("SELECT * FROM GeomacItem WHERE $where ORDER BY mac ASC") { statement ->
            ranges.forEachIndexed { index, range ->
                statement.bindLong(index * 2 + 1, range.first)
                statement.bindLong(index * 2 + 2, range.last)
            }
        }
    }

    private companion object {
        const val TAG = "GeomacRepository"
    }
}
