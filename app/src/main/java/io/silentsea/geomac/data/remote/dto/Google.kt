@file:OptIn(ExperimentalSerializationApi::class)

package io.silentsea.geomac.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoType

@Serializable
data class GoogleRequest(
    @ProtoNumber(1)
    val header: Header? = null,
    @ProtoNumber(4)
    val location: List<Location> = emptyList()
) {
    @Serializable
    data class Header(
        @ProtoNumber(1)
        val version: String? = null,
        @ProtoNumber(2)
        val platform: String? = null,
        @ProtoNumber(5)
        val locale: String? = null,
    )

    @Serializable
    data class Location(
        @ProtoNumber(2)
        val data: Data? = null
    ) {
        @Serializable
        data class Data(
            @ProtoNumber(1)
            @ProtoType(ProtoIntegerType.DEFAULT)
            val timestamp: Long? = null,
            @ProtoNumber(2)
            val wifis: List<Wifi> = emptyList(),
            @ProtoNumber(3)
            @ProtoType(ProtoIntegerType.DEFAULT)
            val size: Int? = null
        ) {
            @Serializable
            data class Wifi(
                @ProtoNumber(1)
                val text: String? = null,
                @ProtoNumber(8)
                @ProtoType(ProtoIntegerType.DEFAULT)
                val mac: Long? = null
            )
        }
    }
}

@Serializable
data class GoogleResponse(
    @ProtoNumber(2)
    val data: Data? = null
) {
    @Serializable
    data class Data(
        @ProtoNumber(3)
        val wifis: List<Wifi> = emptyList()
    ) {
        @Serializable
        data class Wifi(
            @ProtoNumber(1)
            val wifiData: WifiData? = null
        ) {
            @Serializable
            data class WifiData(
                @ProtoNumber(1)
                val location: Location? = null
            ) {
                @Serializable
                data class Location(
                    @ProtoNumber(1)
                    @ProtoType(ProtoIntegerType.FIXED)
                    val latitude: Int? = null,
                    @ProtoNumber(2)
                    @ProtoType(ProtoIntegerType.FIXED)
                    val longitude: Int? = null
                )
            }
        }
    }
}
