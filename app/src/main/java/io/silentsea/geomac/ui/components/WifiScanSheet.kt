package io.silentsea.geomac.ui.components

import android.net.wifi.WifiManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.silentsea.geomac.R
import io.silentsea.geomac.domain.model.Mac
import io.silentsea.geomac.utils.rememberWifiScanResults
import io.silentsea.geomac.utils.signalBars
import io.silentsea.geomac.utils.ssid
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WifiScanSheet(
    onSearch: (Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val wifiManager = remember(context) { context.getSystemService(WifiManager::class.java) }

    val scanResults = rememberWifiScanResults()

    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.wifi_scan),
                    style = MaterialTheme.typography.headlineSmall
                )

                LoadingIndicator(modifier = Modifier.size(40.dp))
            }

            Crossfade(
                targetState = scanResults.isEmpty(),
                modifier = Modifier.weight(1f)
            ) { isEmpty ->
                if (isEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.wifi_scan_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        itemsIndexed(
                            items = scanResults,
                            key = { _, result -> result.BSSID }
                        ) { index, result ->
                            SegmentedListItem(
                                onClick = {
                                    Mac.parse(result.BSSID)?.let(onSearch)

                                    coroutineScope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion { onDismissRequest() }
                                },
                                shapes = ListItemDefaults.segmentedShapes(index, scanResults.size),
                                colors = ListItemDefaults.segmentedColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.animateItem(),
                                leadingContent = {
                                    SignalIcon(bars = wifiManager.signalBars(result))
                                },
                                supportingContent = {
                                    Text(
                                        text = result.BSSID.uppercase(),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            ) {
                                Text(text = result.ssid ?: stringResource(R.string.unknown_ssid))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SignalIcon(bars: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialShapes.Cookie4Sided.toShape())
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        val color = MaterialTheme.colorScheme.onSecondaryContainer

        AnimatedContent(targetState = bars) { bars ->
            Box {
                Icon(
                    painterResource(R.drawable.wifi_24px),
                    contentDescription = null,
                    tint = color.copy(alpha = 0.3f)
                )

                Icon(
                    painterResource(
                        when (bars) {
                            0 -> R.drawable.wifi_1_bar_24px
                            1 -> R.drawable.wifi_2_bar_24px
                            else -> R.drawable.wifi_24px
                        }
                    ),
                    contentDescription = null,
                    tint = color
                )
            }
        }
    }
}
