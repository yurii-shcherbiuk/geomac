package io.silentsea.geomac.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.core.location.LocationManagerCompat
import androidx.core.net.toUri
import io.silentsea.geomac.domain.model.Coordinates

suspend fun Clipboard.copy(label: String, text: String) =
    setClipEntry(ClipEntry(ClipData.newPlainText(label, text)))

val showsClipboardConfirmation: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

fun Context.openMap(coordinates: Coordinates, label: String): Boolean {
    val point = "${coordinates.latitude},${coordinates.longitude}"

    return startActivitySafely(
        Intent(
            Intent.ACTION_VIEW,
            "geo:$point?q=$point(${Uri.encode(label)})".toUri()
        )
    )
}

fun Context.openAppSettings() = startActivitySafely(
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri())
)

fun Context.openLocationSettings() =
    startActivitySafely(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

fun Context.startActivitySafely(intent: Intent): Boolean = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (_: ActivityNotFoundException) {
    false
}

val Context.isLocationEnabled: Boolean
    get() = getSystemService(LocationManager::class.java)
        ?.let(LocationManagerCompat::isLocationEnabled) ?: false

val WifiManager.canScan: Boolean
    @Suppress("DEPRECATION")
    get() = isWifiEnabled || (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && isScanAlwaysAvailable)
