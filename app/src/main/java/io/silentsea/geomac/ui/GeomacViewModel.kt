package io.silentsea.geomac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sink
import io.silentsea.geomac.data.db.entities.GeomacItemWithCoordinates
import io.silentsea.geomac.domain.usecases.DeleteUseCase
import io.silentsea.geomac.domain.usecases.GetAllInRangesUseCase
import io.silentsea.geomac.domain.usecases.GetAllUseCase
import io.silentsea.geomac.domain.usecases.SearchUseCase
import io.silentsea.geomac.domain.usecases.UndoUseCase
import io.silentsea.geomac.utils.toGeomac
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeomacViewModel : ViewModel(), KoinComponent {
    private val searchUseCase: SearchUseCase by inject()
    private val deleteUseCase: DeleteUseCase by inject()
    private val undoUseCase: UndoUseCase by inject()
    private val getAllUseCase: GetAllUseCase by inject()
    private val getAllInRangesUseCase: GetAllInRangesUseCase by inject()


    private val _input = MutableStateFlow("")

    private val _searching = MutableStateFlow(emptySet<Long>())
    val searching = _searching.asStateFlow()

    fun setInput(input: String) {
        _input.update { input }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingFlow = _input
        .flatMapLatest { input ->
            Pager(
                PagingConfig(pageSize = 5)
            ) {
                getAllInRangesUseCase(
                    *input
                        .chunked(12)
                        .map { mac ->
                            mac.padEnd(12, '0').toLong(16) to mac.padEnd(12, 'F').toLong(16)
                        }
                        .toTypedArray()
                )
            }.flow
        }
        .cachedIn(viewModelScope)

    suspend fun search(
        vararg macs: Long = _input.value
            .chunked(12)
            .map { mac ->
                mac
                    .padEnd(12, '0')
                    .toLong(16)
            }
            .toLongArray()
    ) = macs
        .filter { mac -> !_searching.value.contains(mac) }
        .also { filteredMacs ->
            _searching.update { it + filteredMacs }

            searchUseCase(*filteredMacs.toLongArray()).collect { mac ->
                _searching.update { it - mac }
            }
        }


    suspend fun delete(vararg macs: Long) = deleteUseCase(*macs)

    suspend fun undo(vararg items: GeomacItemWithCoordinates) = undoUseCase(*items)

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun saveFile(file: PlatformFile) {
        val data = getAllUseCase().map { it.toGeomac() }

        val json = Json { prettyPrint = true }

        val sink = file.sink(append = false).buffered()

        sink.use { bufferedSink ->
            json.encodeToStream(data, bufferedSink.asOutputStream())
        }
    }
}