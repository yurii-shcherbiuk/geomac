package io.silentsea.geomac.data.db.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import io.silentsea.geomac.domain.model.Service

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
    val service: Service,
    val latitude: Double,
    val longitude: Double
)