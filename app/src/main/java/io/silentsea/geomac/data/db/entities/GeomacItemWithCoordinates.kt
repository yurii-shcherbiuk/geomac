package io.silentsea.geomac.data.db.entities

import androidx.room3.Relation

data class GeomacItemWithCoordinates(
    val mac: Long,
    @Relation(
        parentColumns = ["mac"],
        entityColumns = ["mac"]
    )
    val coordinates: List<GeomacCoordinates>
)