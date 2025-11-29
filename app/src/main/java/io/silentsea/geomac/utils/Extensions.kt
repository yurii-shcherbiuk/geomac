package io.silentsea.geomac.utils

import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import io.silentsea.geomac.data.db.entities.GeomacCoordinates
import java.util.Locale

fun ByteArray.indexOf(array: ByteArray): Int {
    if (array.isEmpty() || array.size > size) return -1

    for (i in 0..size - array.size) {
        if (copyOfRange(i, i + array.size).contentEquals(array)) return i
    }

    return -1
}

fun Long.macString(separator: String = ":"): String = toString(16)
    .padStart(12, '0')
    .uppercase()
    .chunked(2)
    .joinToString(separator)

fun GeomacCoordinates.coordinatesString(): String =
    "${"%.6f".format(Locale.US, latitude)}, ${"%.6f".format(Locale.US, longitude)}"

suspend fun Clipboard.copy(label: String, text: String) =
    setClipEntry(ClipEntry(ClipData.newPlainText(label, text)))

fun Context.showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
