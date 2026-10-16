package io.silentsea.geomac.domain.usecases

import io.silentsea.geomac.domain.repositories.GeomacRepository
import kotlinx.io.RawSource

class ImportUseCase(private val geomacRepository: GeomacRepository) {
    suspend operator fun invoke(source: RawSource) = geomacRepository.import(source)
}
