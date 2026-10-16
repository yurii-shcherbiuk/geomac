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

fun Geomac.toBackup(): BackupEntry {
    fun of(service: Service) =
        locations[service]?.let { BackupCoordinates(it.latitude, it.longitude) }

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

    val locations = buildMap {
        apple?.let { put(Service.APPLE, Coordinates(it.latitude, it.longitude)) }
        google?.let { put(Service.GOOGLE, Coordinates(it.latitude, it.longitude)) }
        microsoft?.let { put(Service.MICROSOFT, Coordinates(it.latitude, it.longitude)) }
        mylnikov?.let { put(Service.MYLNIKOV, Coordinates(it.latitude, it.longitude)) }
        yandex?.let { put(Service.YANDEX, Coordinates(it.latitude, it.longitude)) }
    }

    return Geomac(mac, locations)
}
