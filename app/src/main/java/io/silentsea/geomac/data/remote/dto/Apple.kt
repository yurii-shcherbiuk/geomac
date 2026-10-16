@file:OptIn(ExperimentalSerializationApi::class)

package io.silentsea.geomac.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoType

@Serializable
data class AppleRequest(
    @ProtoNumber(2)
    val wifis: List<Wifi> = emptyList(),
    @ProtoNumber(3)
    @ProtoType(ProtoIntegerType.DEFAULT)
    val noise: Int? = 0,
    @ProtoNumber(4)
    @ProtoType(ProtoIntegerType.DEFAULT)
    val signal: Int? = 100
) {
    @Serializable
    data class Wifi(
        @ProtoNumber(1)
        val mac: String? = null
    )
}

@Serializable
data class AppleResponse(
    @ProtoNumber(2)
    val wifis: List<Wifi> = emptyList()
) {
    @Serializable
    data class Wifi(
        @ProtoNumber(2)
        val location: Location? = null
    ) {
        @Serializable
        data class Location(
            @ProtoNumber(1)
            @ProtoType(ProtoIntegerType.DEFAULT)
            val latitude: Long? = null,
            @ProtoNumber(2)
            @ProtoType(ProtoIntegerType.DEFAULT)
            val longitude: Long? = null
        )
    }
}
