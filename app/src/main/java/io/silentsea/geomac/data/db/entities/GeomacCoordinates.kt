package io.silentsea.geomac.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import io.silentsea.geomac.domain.entites.Services

@Entity(
    primaryKeys = ["mac", "service"],
    foreignKeys = [
        ForeignKey(
            entity = GeomacItem::class,
            parentColumns = ["mac"],
            childColumns = ["mac"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GeomacCoordinates(
    val mac: Long,
    val service: Services,
    val latitude: Double,
    val longitude: Double
)