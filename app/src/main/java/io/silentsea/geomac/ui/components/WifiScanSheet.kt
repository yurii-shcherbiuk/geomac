package io.silentsea.geomac.ui.components

import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.silentsea.geomac.R
import io.silentsea.geomac.utils.rememberWifiScanResults
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WifiScanSheet(
    onSearch: (Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    val wifiManager = context.getSystemService(WifiManager::class.java)

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
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.wifi_scan),
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedCard(
                modifier = Modifier.weight(1f)
            ) {
                Crossfade(
                    targetState = scanResults.isNotEmpty()
                ) { isNotEmpty ->
                    if (isNotEmpty) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                count = scanResults.size,
                                key = { index -> scanResults[index].BSSID }
                            ) { index ->
                                Column(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .animateItem()
                                ) {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    scanResults[index].wifiSsid?.toString()
                                                        ?.trim('"')
                                                        ?.takeIf { it.isNotEmpty() }
                                                        ?: stringResource(R.string.unknown_ssid)
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    scanResults[index].SSID.takeIf { it.isNotEmpty() }
                                                        ?: stringResource(R.string.unknown_ssid)
                                                }
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = scanResults[index].BSSID.uppercase()
                                            )
                                        },
                                        trailingContent = {
                                            AnimatedContent(
                                                targetState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                    (wifiManager.calculateSignalLevel(scanResults[index].level) / wifiManager.maxSignalLevel.toFloat() * 2).roundToInt()
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    (WifiManager.calculateSignalLevel(
                                                        scanResults[index].level,
                                                        Integer.MAX_VALUE
                                                    ) / Integer.MAX_VALUE.minus(1)
                                                        .toFloat() * 2).roundToInt()
                                                }
                                            ) { level ->
                                                Box {
                                                    Icon(
                                                        painterResource(R.drawable.wifi_24px),
                                                        contentDescription = null,
                                                        tint = LocalContentColor.current.copy(alpha = 0.3f)
                                                    )

                                                    Icon(
                                                        painterResource(
                                                            when (level) {
                                                                0 -> R.drawable.wifi_1_bar_24px
                                                                1 -> R.drawable.wifi_2_bar_24px
                                                                else -> R.drawable.wifi_24px
                                                            }
                                                        ),
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSearch(
                                                    scanResults[index].BSSID
                                                        .uppercase()
                                                        .filter { it.isDigit() || it in 'A'..'F' }
                                                        .toLong(16)
                                                )

                                                coroutineScope.launch {
                                                    sheetState.hide()

                                                    onDismissRequest()
                                                }
                                            }
                                    )

                                    AnimatedVisibility(
                                        visible = index < scanResults.lastIndex
                                    ) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                }
            }
        }
    }
}