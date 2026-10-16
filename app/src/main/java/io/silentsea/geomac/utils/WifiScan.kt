package io.silentsea.geomac.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("MissingPermission")
@Composable
fun rememberWifiScanResults(intervalMillis: Long = 10_000L): List<ScanResult> {
    val context = LocalContext.current
    val wifiManager = remember(context) { context.getSystemService(WifiManager::class.java) }

    var scanResults by remember { mutableStateOf(emptyList<ScanResult>()) }

    fun update() {
        scanResults = wifiManager.scanResults
            .distinctBy { it.BSSID }
            .sortedByDescending { it.level }
    }

    DisposableEffect(wifiManager) {
        update()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val callback = object : WifiManager.ScanResultsCallback() {
                override fun onScanResultsAvailable() = update()
            }

            wifiManager.registerScanResultsCallback(context.mainExecutor, callback)

            onDispose { runCatching { wifiManager.unregisterScanResultsCallback(callback) } }
        } else {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) = update()
            }

            context.registerReceiver(
                receiver,
                IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            )

            onDispose { runCatching { context.unregisterReceiver(receiver) } }
        }
    }

    LaunchedEffect(wifiManager, intervalMillis) {
        while (true) {
            @Suppress("DEPRECATION")
            wifiManager.startScan()
            delay(intervalMillis.milliseconds)
        }
    }

    return scanResults
}

val ScanResult.ssid: String?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        wifiSsid?.toString()?.removeSurrounding("\"")
    } else {
        @Suppress("DEPRECATION")
        SSID
    }?.takeIf { it.isNotBlank() }

fun WifiManager.signalBars(result: ScanResult): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        calculateSignalLevel(result.level) * 2 / maxSignalLevel.coerceAtLeast(1)
    } else {
        @Suppress("DEPRECATION")
        WifiManager.calculateSignalLevel(result.level, 3)
    }
