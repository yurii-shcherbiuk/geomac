package io.silentsea.geomac.data.remote.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("GetLocationUsingFingerprint", "http://inference.location.live.com")
data class MicrosoftRequest(
    @XmlSerialName("RequestHeader")
    @XmlElement
    val requestHeader: RequestHeader,
    @XmlSerialName("BeaconFingerprint")
    @XmlElement
    val beaconFingerprint: BeaconFingerprint
) {
    @Serializable
    data class RequestHeader(
        @XmlSerialName("Timestamp")
        @XmlElement
        val timestamp: LocalDateTime,
        @XmlSerialName("ApplicationId")
        @XmlElement
        val applicationId: String,
        @XmlSerialName("TrackingId")
        @XmlElement
        val trackingId: String,
        @XmlSerialName("DeviceProfile")
        @XmlElement
        val deviceProfile: DeviceProfile,
        @XmlSerialName("Authorization")
        @XmlElement
        val authorization: Authorization = Authorization
    ) {
        @Serializable
        data class DeviceProfile(
            @XmlSerialName("ClientGuid")
            @XmlElement(false)
            val clientGuid: String,
            @XmlSerialName("Platform")
            @XmlElement(false)
            val platform: String,
            @XmlSerialName("DeviceType")
            @XmlElement(false)
            val deviceType: String,
            @XmlSerialName("OSVersion")
            @XmlElement(false)
            val osVersion: String,
            @XmlSerialName("LFVersion")
            @XmlElement(false)
            val lfVersion: String,
            @XmlSerialName("ExtendedDeviceInfo")
            @XmlElement(false)
            val extendedDeviceInfo: String
        )

        @Serializable
        object Authorization
    }

    @Serializable
    data class BeaconFingerprint(
        @XmlSerialName("Detections")
        @XmlElement
        val detections: Detections
    ) {
        @Serializable
        data class Detections(
            @XmlSerialName("Wifi7")
            @XmlElement
            val wifi7: Wifi7
        ) {
            @Serializable
            data class Wifi7(
                @XmlSerialName("BssId")
                @XmlElement(false)
                val bssId: String,
                @XmlSerialName("rssi")
                @XmlElement(false)
                val rssi: String
            )
        }
    }
}

@Serializable
@XmlSerialName("GetLocationUsingFingerprintResponse", "http://inference.location.live.com")
data class MicrosoftResponse(
    @XmlSerialName("GetLocationUsingFingerprintResult")
    @XmlElement
    val result: GetLocationUsingFingerprintResult? = null
) {
    @Serializable
    data class GetLocationUsingFingerprintResult(
        @XmlSerialName("ResponseStatus")
        @XmlElement
        val responseStatus: String? = null,
        @XmlSerialName("LocationResult")
        @XmlElement
        val locationResult: LocationResult? = null
    ) {
        @Serializable
        data class LocationResult(
            @XmlSerialName("ResolverStatus")
            @XmlElement
            val resolverStatus: ResolverStatus? = null,
            @XmlSerialName("ResolvedPosition")
            @XmlElement
            val resolvedPosition: ResolvedPosition? = null,
            @XmlSerialName("RadialUncertainty")
            @XmlElement
            val radialUncertainty: Int? = null
        ) {
            @Serializable
            data class ResolverStatus(
                @XmlSerialName("Status")
                @XmlElement(false)
                val status: String? = null,
                @XmlSerialName("Source")
                @XmlElement(false)
                val source: String? = null
            )

            @Serializable
            data class ResolvedPosition(
                @XmlSerialName("Latitude")
                @XmlElement(false)
                val latitude: String? = null,
                @XmlSerialName("Longitude")
                @XmlElement(false)
                val longitude: String? = null
            )
        }
    }
}
