package io.silentsea.geomac.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.silentsea.geomac.R
import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Geomac
import io.silentsea.geomac.domain.model.Mac
import io.silentsea.geomac.domain.model.Service
import io.silentsea.geomac.domain.model.macString
import io.silentsea.geomac.ui.components.EmptyState
import io.silentsea.geomac.ui.components.ErrorSheet
import io.silentsea.geomac.ui.components.GeomacCard
import io.silentsea.geomac.ui.components.SearchField
import io.silentsea.geomac.ui.components.WifiScanSheet
import io.silentsea.geomac.ui.model.labelRes
import io.silentsea.geomac.utils.canScan
import io.silentsea.geomac.utils.isLocationEnabled
import io.silentsea.geomac.utils.openAppSettings
import io.silentsea.geomac.utils.openLocationSettings
import io.silentsea.geomac.utils.openMap
import io.silentsea.geomac.utils.showsClipboardConfirmation
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Geomac(viewModel: GeomacViewModel = koinViewModel()) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val searching by viewModel.searching.collectAsStateWithLifecycle()
    val lazyPagingItems = viewModel.items.collectAsLazyPagingItems()

    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    var isSearchFocused by remember { mutableStateOf(false) }

    val canSearchQuery by remember {
        derivedStateOf { textFieldState.text.length >= Mac.HEX_LENGTH }
    }

    var error by remember { mutableStateOf<Throwable?>(null) }

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        onAction: () -> Unit = {}
    ) {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = actionLabel == null,
                duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) onAction()
        }
    }

    fun clearFocus() = focusManager.clearFocus()

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collect(viewModel::onQueryChange)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                GeomacEvent.InvalidMac -> showSnackbar(resources.getString(R.string.invalid_mac))

                is GeomacEvent.NotFound -> showSnackbar(
                    resources.getString(R.string.mac_not_found, event.mac.macString())
                )

                is GeomacEvent.Deleted -> showSnackbar(
                    message = resources.getString(R.string.mac_deleted, event.item.mac.macString()),
                    actionLabel = resources.getString(R.string.undo)
                ) { viewModel.undo(event.item) }

                is GeomacEvent.Exported -> showSnackbar(
                    resources.getQuantityString(R.plurals.exported, event.count, event.count)
                )

                is GeomacEvent.Imported -> showSnackbar(
                    resources.getQuantityString(R.plurals.imported, event.count, event.count)
                )

                is GeomacEvent.Failed -> showSnackbar(
                    message = event.error.localizedMessage ?: resources.getString(R.string.error),
                    actionLabel = resources.getString(R.string.details)
                ) { error = event.error }
            }
        }
    }

    val wifiManager = remember(context) { context.getSystemService(WifiManager::class.java) }
    var isWifiScanSheetOpened by rememberSaveable { mutableStateOf(false) }
    var isWaitingForWifi by rememberSaveable { mutableStateOf(false) }

    fun openWifiScan() {
        when {
            !context.isLocationEnabled -> showSnackbar(
                message = resources.getString(R.string.turn_on_location),
                actionLabel = resources.getString(R.string.go_to_settings)
            ) { context.openLocationSettings() }

            wifiManager.canScan -> isWifiScanSheetOpened = true

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                isWaitingForWifi = true
                showSnackbar(resources.getString(R.string.turn_on_wifi))
                context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
            }

            else -> {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = true
                isWifiScanSheetOpened = true
            }
        }
    }

    LifecycleResumeEffect(isWaitingForWifi) {
        if (isWaitingForWifi && wifiManager.canScan) {
            isWaitingForWifi = false
            isWifiScanSheetOpened = true
        }

        onPauseOrDispose {}
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openWifiScan()
            } else {
                showSnackbar(
                    message = resources.getString(R.string.permission_description),
                    actionLabel = resources.getString(R.string.go_to_settings)
                ) { context.openAppSettings() }
            }
        }

    fun onWifiScanClick() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openWifiScan()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    val exportLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file -> file?.let(viewModel::export) }
    val importLauncher = rememberFilePickerLauncher { file -> file?.let(viewModel::import) }

    val searchScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )

    val bottomContentPadding =
        FloatingToolbarDefaults.ContainerSize + FloatingToolbarDefaults.ScreenOffset * 2

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(searchScrollBehavior.nestedScrollConnection)
            .nestedScroll(toolbarScrollBehavior),
        topBar = {
            AppBarWithSearch(
                state = searchBarState,
                scrollBehavior = searchScrollBehavior,
                inputField = {
                    SearchField(
                        textFieldState = textFieldState,
                        searchBarState = searchBarState,
                        onSearch = {
                            clearFocus()
                            viewModel.searchQuery()
                        },
                        modifier = Modifier.onFocusChanged { isSearchFocused = it.isFocused }
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = bottomContentPadding)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .pointerInput(Unit) { detectTapGestures { clearFocus() } }
        ) {
            GeomacContent(
                lazyPagingItems = lazyPagingItems,
                searching = searching,
                hasQuery = textFieldState.text.isNotEmpty(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + bottomContentPadding
                ),
                onRefresh = viewModel::search,
                onDelete = viewModel::delete,
                onOpenMap = { item, service, coordinates ->
                    val label = "${resources.getString(service.labelRes)} · ${item.mac.macString()}"

                    if (!context.openMap(coordinates, label)) {
                        showSnackbar(resources.getString(R.string.no_maps_app))
                    }
                },
                onCopied = { message ->
                    if (!showsClipboardConfirmation) showSnackbar(message)
                },
                onShowError = { error = it }
            )

            HorizontalFloatingToolbar(
                expanded = true,
                scrollBehavior = toolbarScrollBehavior,
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .offset(y = -FloatingToolbarDefaults.ScreenOffset),
                floatingActionButton = {
                    FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = ::onWifiScanClick) {
                        Icon(
                            painterResource(R.drawable.wifi_find_24px),
                            contentDescription = stringResource(R.string.wifi_scan)
                        )
                    }
                }
            ) {
                IconButton(
                    onClick = {
                        clearFocus()
                        viewModel.searchQuery()
                    },
                    enabled = canSearchQuery
                ) {
                    Icon(
                        painterResource(R.drawable.travel_explore_24px),
                        contentDescription = stringResource(R.string.mac_search)
                    )
                }

                IconButton(onClick = { importLauncher.launch() }) {
                    Icon(
                        painterResource(R.drawable.file_open_24px),
                        contentDescription = stringResource(R.string.import_backup)
                    )
                }

                IconButton(
                    onClick = {
                        exportLauncher.launch(
                            suggestedName = "geomac",
                            defaultExtension = "json",
                            allowedExtensions = setOf("json")
                        )
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.save_as_24px),
                        contentDescription = stringResource(R.string.export_backup)
                    )
                }
            }
        }
    }

    BackHandler(enabled = isSearchFocused) { clearFocus() }

    error?.let {
        ErrorSheet(
            error = it,
            onDismissRequest = { error = null }
        )
    }

    if (isWifiScanSheetOpened) {
        WifiScanSheet(
            onSearch = { mac ->
                textFieldState.setTextAndPlaceCursorAtEnd(mac.macString(""))
                viewModel.search(mac)
            },
            onDismissRequest = { isWifiScanSheetOpened = false }
        )
    }
}

private enum class ContentState { Loading, Error, Empty, Items }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GeomacContent(
    lazyPagingItems: LazyPagingItems<Geomac>,
    searching: Set<Long>,
    hasQuery: Boolean,
    contentPadding: PaddingValues,
    onRefresh: (Long) -> Unit,
    onDelete: (Geomac) -> Unit,
    onOpenMap: (Geomac, Service, Coordinates) -> Unit,
    onCopied: (String) -> Unit,
    onShowError: (Throwable) -> Unit
) {
    val refresh = lazyPagingItems.loadState.refresh

    val state = when {
        lazyPagingItems.itemCount > 0 -> ContentState.Items
        refresh is LoadState.Error -> ContentState.Error
        refresh is LoadState.Loading -> ContentState.Loading
        else -> ContentState.Empty
    }

    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = Modifier.fillMaxSize()
    ) { target ->
        when (target) {
            ContentState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ContainedLoadingIndicator()
            }

            ContentState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ErrorMessage(onShowStackTrace = {
                    (refresh as? LoadState.Error)?.let {
                        onShowError(
                            it.error
                        )
                    }
                })
            }

            ContentState.Empty -> EmptyState(
                icon = painterResource(R.drawable.travel_explore_24px),
                title = stringResource(if (hasQuery) R.string.no_matches else R.string.empty),
                description = stringResource(if (hasQuery) R.string.no_matches_hint else R.string.empty_hint),
                modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())
            )

            ContentState.Items -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey { it.mac }
                ) { index ->
                    lazyPagingItems[index]?.let { item ->
                        GeomacCard(
                            item = item,
                            isSearching = item.mac in searching,
                            onRefresh = { onRefresh(item.mac) },
                            onDelete = { onDelete(item) },
                            onOpenMap = { service, coordinates ->
                                onOpenMap(
                                    item,
                                    service,
                                    coordinates
                                )
                            },
                            onCopied = onCopied,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                when (val append = lazyPagingItems.loadState.append) {
                    is LoadState.Error -> item(key = "append-error") {
                        ErrorMessage(
                            onShowStackTrace = { onShowError(append.error) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        )
                    }

                    is LoadState.Loading -> item(key = "append-loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }

                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    onShowStackTrace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.error),
            style = MaterialTheme.typography.headlineSmall
        )

        TextButton(onClick = onShowStackTrace) {
            Text(stringResource(R.string.show_stack_trace))
        }
    }
}
