package io.silentsea.geomac.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MylnikovResponse(
    val data: Data? = null
) {
    @Serializable
    data class Data(
        val lat: Double? = null,
        val lon: Double? = null
    )
}
