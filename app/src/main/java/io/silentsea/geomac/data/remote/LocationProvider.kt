package io.silentsea.geomac.data.remote

import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Service

interface LocationProvider {
    val service: Service

    suspend fun locate(mac: Long): Coordinates?
}
