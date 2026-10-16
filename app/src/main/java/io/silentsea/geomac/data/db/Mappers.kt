package io.silentsea.geomac.data.db

import io.silentsea.geomac.data.db.entities.GeomacCoordinates
import io.silentsea.geomac.data.db.entities.GeomacItemWithCoordinates
import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Geomac

fun GeomacItemWithCoordinates.toDomain() = Geomac(
    mac = mac,
    locations = coordinates.associate { it.service to Coordinates(it.latitude, it.longitude) }
)

fun Geomac.toEntity() = GeomacItemWithCoordinates(
    mac = mac,
    coordinates = locations.map { (service, coordinates) ->
        GeomacCoordinates(
            mac = mac,
            service = service,
            latitude = coordinates.latitude,
            longitude = coordinates.longitude
        )
    }
)
