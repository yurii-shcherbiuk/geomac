package io.silentsea.geomac.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.silentsea.geomac.core.Const
import io.silentsea.geomac.data.remote.dto.MicrosoftRequest
import io.silentsea.geomac.data.remote.dto.MicrosoftResponse
import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Service
import io.silentsea.geomac.domain.model.macString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.time.Clock

class MicrosoftProvider(private val client: HttpClient) : LocationProvider {
    override val service = Service.MICROSOFT

    override suspend fun locate(mac: Long): Coordinates? {
        val request = XmlFormat.encodeToString(
            MicrosoftRequest(
                requestHeader = MicrosoftRequest.RequestHeader(
                    timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    applicationId = "e1e71f6b-2149-45f3-a298-a20682ab5017",
                    trackingId = "21BF9AD6-CFD3-46B3-B041-EE90BD34FDBC",
                    deviceProfile = MicrosoftRequest.RequestHeader.DeviceProfile(
                        clientGuid = "0fc571be-4624-4ce0-b04e-911bdeb1a222",
                        platform = "Windows7",
                        deviceType = "PC",
                        osVersion = "7600.16695.amd64fre.win7_gdr.101026-1503",
                        lfVersion = "9.0.8080.16413"
                    )
                ),
                beaconFingerprint = MicrosoftRequest.BeaconFingerprint(
                    detections = MicrosoftRequest.BeaconFingerprint.Detections(
                        wifi7 = MicrosoftRequest.BeaconFingerprint.Detections.Wifi7(
                            bssId = mac.macString()
                        )
                    )
                )
            )
        )

        val data = client
            .post(Const.MICROSOFT) {
                contentType(ContentType.Application.Xml)
                setBody(request)
            }
            .body<String>()

        val result = XmlFormat.decodeFromString<MicrosoftResponse>(data).result
        val location = result?.locationResult ?: return null

        val isReliable = result.responseStatus == "Success" &&
                location.resolverStatus?.status == "Success" &&
                location.resolverStatus.source == "Internal" &&
                (location.radialUncertainty ?: Int.MAX_VALUE) < MAX_UNCERTAINTY_METERS

        if (!isReliable) return null

        val latitude = location.resolvedPosition?.latitude?.toDoubleOrNull() ?: return null
        val longitude = location.resolvedPosition.longitude?.toDoubleOrNull() ?: return null

        return Coordinates(latitude, longitude)
    }

    private companion object {
        const val MAX_UNCERTAINTY_METERS = 500
    }
}
