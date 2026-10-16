package io.silentsea.geomac.domain.usecases

import io.silentsea.geomac.domain.model.Geomac
import io.silentsea.geomac.domain.repositories.GeomacRepository

class UndoUseCase(private val geomacRepository: GeomacRepository) {
    suspend operator fun invoke(item: Geomac) = geomacRepository.restore(item)
}
