package io.silentsea.geomac.data.backup

import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Geomac
import io.silentsea.geomac.domain.model.Mac
import io.silentsea.geomac.domain.model.Service
import io.silentsea.geomac.domain.model.macString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BackupEntry(
    val mac: String,
    val apple: BackupCoordinates? = null,
    val google: BackupCoordinates? = null,
    val microsoft: BackupCoordinates? = null,
    val mylnikov: BackupCoordinates? = null,
    val yandex: BackupCoordinates? = null
)

@Serializable
data class BackupCoordinates(
    val latitude: Double,
    val longitude: Double
)

val BackupJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    explicitNulls = false
}

private fun Coordinates.toBackup() = BackupCoordinates(latitude, longitude)

private fun BackupCoordinates.toDomain() = Coordinates(latitude, longitude)

fun Geomac.toBackup(): BackupEntry {
    fun of(service: Service) = locations[service]?.toBackup()

    return BackupEntry(
        mac = mac.macString(),
        apple = of(Service.APPLE),
        google = of(Service.GOOGLE),
        microsoft = of(Service.MICROSOFT),
        mylnikov = of(Service.MYLNIKOV),
        yandex = of(Service.YANDEX)
    )
}

fun BackupEntry.toDomain(): Geomac? {
    val mac = Mac.parse(mac) ?: return null

    val locations = mapOf(
        Service.APPLE to apple,
        Service.GOOGLE to google,
        Service.MICROSOFT to microsoft,
        Service.MYLNIKOV to mylnikov,
        Service.YANDEX to yandex
    ).mapNotNull { (service, coordinates) -> coordinates?.let { service to it.toDomain() } }

    return Geomac(mac, locations.toMap())
}
