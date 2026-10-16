package io.silentsea.geomac.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.silentsea.geomac.core.Const
import io.silentsea.geomac.data.remote.dto.YandexResponse
import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Service
import io.silentsea.geomac.domain.model.macString
import kotlinx.serialization.decodeFromString

class YandexProvider(private val client: HttpClient) : LocationProvider {
    override val service = Service.YANDEX

    override suspend fun locate(mac: Long): Coordinates? {
        val data = try {
            client
                .get(Const.YANDEX) {
                    parameter("wifinetworks", "${mac.macString("")}:-65")
                }
                .body<String>()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) return null else throw e
        }

        val response = XmlFormat.decodeFromString<YandexResponse>(data)

        if (response.source != "FoundByWifi") return null

        val latitude = response.coordinates?.latitude?.toDoubleOrNull() ?: return null
        val longitude = response.coordinates.longitude?.toDoubleOrNull() ?: return null

        return Coordinates(latitude, longitude)
    }
}
