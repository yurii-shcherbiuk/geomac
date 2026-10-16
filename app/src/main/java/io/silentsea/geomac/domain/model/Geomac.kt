package io.silentsea.geomac.domain.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class Geomac(
    val mac: Long,
    val locations: Map<Service, Coordinates>
)
