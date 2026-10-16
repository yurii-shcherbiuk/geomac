package io.silentsea.geomac.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.silentsea.geomac.core.Const
import io.silentsea.geomac.data.remote.dto.AppleRequest
import io.silentsea.geomac.data.remote.dto.AppleResponse
import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Service
import io.silentsea.geomac.domain.model.macString
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

class AppleProvider(private val client: HttpClient) : LocationProvider {
    override val service = Service.APPLE

    override suspend fun locate(mac: Long): Coordinates? {
        val request = ProtoFormat.encodeToByteArray(
            AppleRequest(wifis = listOf(AppleRequest.Wifi(mac = mac.macString())))
        )

        val data = client.post(Const.APPLE) { setBody(appleRequestBody(request)) }.body<ByteArray>()

        val index = data.indexOf(RESPONSE_MARKER)
        check(index != -1) { "Unexpected Apple response" }

        val response = ProtoFormat.decodeFromByteArray<AppleResponse>(
            data.copyOfRange(index + RESPONSE_MARKER.size + 2, data.size)
        )

        return response.wifis
            .firstNotNullOfOrNull { wifi ->
                val latitude = wifi.location?.latitude
                val longitude = wifi.location?.longitude

                if (latitude == null || longitude == null || latitude == NOT_FOUND || longitude == NOT_FOUND) {
                    null
                } else {
                    Coordinates(latitude * SCALE, longitude * SCALE)
                }
            }
    }

    private companion object {
        val RESPONSE_MARKER = byteArrayOf(0x00, 0x00, 0x00, 0x01, 0x00, 0x00)
        const val NOT_FOUND = -18_000_000_000L
        const val SCALE = 1e-8
    }
}

internal fun appleRequestBody(request: ByteArray) = buildBytes {
    writeShort(1)
    writeUTF("en_US")
    writeUTF("com.apple.locationd")
    writeUTF("8.4.1.12H321")
    writeInt(1)
    writeShort(0)
    writeShort(request.size)
    write(request)
}
