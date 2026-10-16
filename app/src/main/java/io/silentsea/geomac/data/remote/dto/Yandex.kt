package io.silentsea.geomac.data.remote.dto

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("location")
data class YandexResponse(
    @XmlSerialName("source")
    @XmlElement(false)
    val source: String? = null,
    @XmlSerialName("coordinates")
    @XmlElement
    val coordinates: Coordinates? = null
) {
    @Serializable
    data class Coordinates(
        @XmlSerialName("latitude")
        @XmlElement(false)
        val latitude: String? = null,
        @XmlSerialName("longitude")
        @XmlElement(false)
        val longitude: String? = null
    )
}
