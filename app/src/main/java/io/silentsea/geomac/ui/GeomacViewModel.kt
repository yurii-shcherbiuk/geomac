package io.silentsea.geomac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sink
import io.github.vinceglb.filekit.source
import io.silentsea.geomac.domain.model.Geomac
import io.silentsea.geomac.domain.model.Mac
import io.silentsea.geomac.domain.usecases.DeleteUseCase
import io.silentsea.geomac.domain.usecases.ExportUseCase
import io.silentsea.geomac.domain.usecases.GetAllInRangesUseCase
import io.silentsea.geomac.domain.usecases.ImportUseCase
import io.silentsea.geomac.domain.usecases.SearchUseCase
import io.silentsea.geomac.domain.usecases.UndoUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface GeomacEvent {
    data object InvalidMac : GeomacEvent
    data class NotFound(val mac: Long) : GeomacEvent
    data class Deleted(val item: Geomac) : GeomacEvent
    data class Exported(val count: Int) : GeomacEvent
    data class Imported(val count: Int) : GeomacEvent
    data class Failed(val error: Throwable) : GeomacEvent
}

class GeomacViewModel(
    private val searchUseCase: SearchUseCase,
    private val deleteUseCase: DeleteUseCase,
    private val undoUseCase: UndoUseCase,
    getAllInRangesUseCase: GetAllInRangesUseCase,
    private val exportUseCase: ExportUseCase,
    private val importUseCase: ImportUseCase
) : ViewModel() {
    private val query = MutableStateFlow("")

    private val _searching = MutableStateFlow(emptySet<Long>())
    val searching: StateFlow<Set<Long>> = _searching.asStateFlow()

    private val _events = Channel<GeomacEvent>(Channel.BUFFERED)
    val events: Flow<GeomacEvent> = _events.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: Flow<PagingData<Geomac>> = query
        .map(Mac::prefixRanges)
        .distinctUntilChanged()
        .flatMapLatest { ranges -> getAllInRangesUseCase(ranges) }
        .cachedIn(viewModelScope)

    fun onQueryChange(input: String) {
        query.value = input
    }

    fun searchQuery() {
        val macs = Mac.parseAll(query.value)

        if (macs.isEmpty()) {
            _events.trySend(GeomacEvent.InvalidMac)
        } else {
            macs.forEach(::search)
        }
    }

    fun search(mac: Long) {
        var isNew = false
        _searching.update { searching ->
            isNew = mac !in searching
            if (isNew) searching + mac else searching
        }
        if (!isNew) return

        launchCatching(onFinally = { _searching.update { it - mac } }) {
            if (searchUseCase(mac) == 0) GeomacEvent.NotFound(mac) else null
        }
    }

    fun delete(item: Geomac) = launchCatching {
        deleteUseCase(item.mac)
        GeomacEvent.Deleted(item)
    }

    fun undo(item: Geomac) = launchCatching {
        undoUseCase(item)
        null
    }

    fun export(file: PlatformFile) = launchCatching {
        GeomacEvent.Exported(withContext(Dispatchers.IO) { exportUseCase(file.sink(append = false)) })
    }

    fun import(file: PlatformFile) = launchCatching {
        GeomacEvent.Imported(withContext(Dispatchers.IO) { importUseCase(file.source()) })
    }

    private fun launchCatching(
        onFinally: () -> Unit = {},
        block: suspend () -> GeomacEvent?
    ) {
        viewModelScope.launch {
            val event = try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                GeomacEvent.Failed(e)
            } finally {
                onFinally()
            }

            event?.let { _events.send(it) }
        }
    }
}
