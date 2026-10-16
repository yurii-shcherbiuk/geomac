package io.silentsea.geomac.domain.model

import java.util.Locale

object Mac {
    const val HEX_LENGTH = 12
    const val MAX = 0xFFFFFFFFFFFF

    val RANGE = 0L..MAX

    fun isHexDigit(char: Char): Boolean = char.isDigit() || char.uppercaseChar() in 'A'..'F'

    fun sanitize(input: CharSequence): String = buildString(input.length) {
        input.forEach { char -> if (isHexDigit(char)) append(char.uppercaseChar()) }
    }

    fun parseAll(input: String): List<Long> = sanitize(input)
        .chunked(HEX_LENGTH)
        .filter { chunk -> chunk.length == HEX_LENGTH }
        .map { chunk -> chunk.toLong(16) }
        .distinct()

    fun parse(input: String): Long? = sanitize(input)
        .takeIf { it.length == HEX_LENGTH }
        ?.toLong(16)

    fun prefixRanges(input: String): List<LongRange> = sanitize(input)
        .chunked(HEX_LENGTH)
        .map { prefix ->
            prefix.padEnd(HEX_LENGTH, '0').toLong(16)..prefix.padEnd(HEX_LENGTH, 'F').toLong(16)
        }
        .merged()
}

fun List<LongRange>.merged(): List<LongRange> = sortedBy { it.first }
    .fold(mutableListOf()) { acc, range ->
        val last = acc.lastOrNull()

        if (last != null && range.first <= last.last + 1) {
            acc[acc.lastIndex] = last.first..maxOf(last.last, range.last)
        } else {
            acc += range
        }

        acc
    }

fun Long.macString(separator: String = ":"): String = toString(16)
    .padStart(Mac.HEX_LENGTH, '0')
    .uppercase(Locale.ROOT)
    .chunked(2)
    .joinToString(separator)

fun Coordinates.coordinatesString(): String =
    "%.6f, %.6f".format(Locale.ROOT, latitude, longitude)
