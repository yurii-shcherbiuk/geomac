package io.silentsea.geomac.domain.entites

import kotlinx.serialization.Serializable

@Serializable
data class Geomac(
    val mac: String,
    val apple: Coordinates? = null,
    val google: Coordinates? = null,
    val microsoft: Coordinates? = null,
    val mylnikov: Coordinates? = null,
    val yandex: Coordinates? = null
) {
    @Serializable
    data class Coordinates(
        val latitude: Double,
        val longitude: Double
    )
}
