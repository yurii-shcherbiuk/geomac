package io.silentsea.geomac.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.silentsea.geomac.core.Const
import io.silentsea.geomac.data.remote.dto.GoogleRequest
import io.silentsea.geomac.data.remote.dto.GoogleResponse
import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Service
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

class GoogleProvider(private val client: HttpClient) : LocationProvider {
    override val service = Service.GOOGLE

    override suspend fun locate(mac: Long): Coordinates? {
        val request = ProtoFormat.encodeToByteArray(
            GoogleRequest(
                header = GoogleRequest.Header(
                    version = "2021",
                    platform = "android/LEAGOO/full_wf562g_leagoo/wf562g_leagoo:6.0/MRA58K/1511161770:user/release-keys",
                    locale = "en_US"
                ),
                location = listOf(
                    GoogleRequest.Location(
                        data = GoogleRequest.Location.Data(
                            timestamp = 162723L,
                            wifis = listOf(
                                GoogleRequest.Location.Data.Wifi(text = "", mac = mac),
                                GoogleRequest.Location.Data.Wifi(text = "", mac = DUMMY_MAC)
                            ),
                            size = 2
                        )
                    )
                )
            )
        ).gzip()

        val data =
            client.post(Const.GOOGLE) { setBody(googleRequestBody(request)) }.body<ByteArray>()

        val index = data.indexOf(GZIP_MAGIC)
        check(index != -1) { "Unexpected Google response" }

        val response = ProtoFormat.decodeFromByteArray<GoogleResponse>(
            data.copyOfRange(index, data.size).gunzip()
        )

        return response.data?.wifis
            ?.firstNotNullOfOrNull { wifi ->
                val latitude = wifi.wifiData?.location?.latitude
                val longitude = wifi.wifiData?.location?.longitude

                if (latitude == null || longitude == null) null
                else Coordinates(latitude * SCALE, longitude * SCALE)
            }
    }

    private companion object {
        const val DUMMY_MAC = 0x112233445566L
        val GZIP_MAGIC = byteArrayOf(0x1f, 0x8b.toByte())
        const val SCALE = 1e-7
    }
}

internal fun googleRequestBody(gzippedRequest: ByteArray) = buildBytes {
    writeShort(2)
    writeByte(0)
    writeUTF("location,2021,android,gms,en_US")
    writeLong(0)
    writeUTF("g")
    writeInt(187)
    writeBytes(0x00, 0x01, 0x01, 0x00, 0x01)
    writeUTF("g:loc/ql")
    writeShort(0)
    writeUTF("POST")
    writeBytes(0x6d, 0x72, 0x00, 0x00)
    writeUTF("ROOT")
    writeByte(0)
    writeInt(gzippedRequest.size)
    writeUTF("g")
    write(gzippedRequest)
    writeShort(0)
}
