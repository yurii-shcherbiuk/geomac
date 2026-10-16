package io.silentsea.geomac.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.silentsea.geomac.core.Const
import io.silentsea.geomac.data.remote.dto.MylnikovResponse
import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Service
import io.silentsea.geomac.domain.model.macString

class MylnikovProvider(private val client: HttpClient) : LocationProvider {
    override val service = Service.MYLNIKOV

    override suspend fun locate(mac: Long): Coordinates? {
        val data = client
            .get(Const.MYLNIKOV) {
                parameter("v", "1.1")
                parameter("data", "open")
                parameter("bssid", mac.macString())
            }
            .body<String>()

        val response = JsonFormat.decodeFromString<MylnikovResponse>(data).data

        val latitude = response?.lat ?: return null
        val longitude = response.lon ?: return null

        return Coordinates(latitude, longitude)
    }
}
