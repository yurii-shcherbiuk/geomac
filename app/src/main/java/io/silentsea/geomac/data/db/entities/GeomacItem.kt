package io.silentsea.geomac.data.db.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class GeomacItem(
    @PrimaryKey
    val mac: Long,
)