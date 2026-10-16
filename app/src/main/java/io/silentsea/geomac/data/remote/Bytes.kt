package io.silentsea.geomac.data.remote

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

internal inline fun buildBytes(block: DataOutputStream.() -> Unit): ByteArray =
    ByteArrayOutputStream().also { DataOutputStream(it).use(block) }.toByteArray()

internal fun ByteArray.indexOf(pattern: ByteArray, startIndex: Int = 0): Int {
    if (pattern.isEmpty() || pattern.size > size) return -1

    outer@ for (i in startIndex..size - pattern.size) {
        for (j in pattern.indices) {
            if (this[i + j] != pattern[j]) continue@outer
        }

        return i
    }

    return -1
}

internal fun ByteArray.gzip(): ByteArray =
    ByteArrayOutputStream().also { out -> GZIPOutputStream(out).use { it.write(this) } }
        .toByteArray()

internal fun ByteArray.gunzip(): ByteArray = GZIPInputStream(inputStream()).use { it.readBytes() }
