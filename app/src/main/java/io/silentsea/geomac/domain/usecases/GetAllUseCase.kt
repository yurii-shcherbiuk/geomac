package io.silentsea.geomac.domain.usecases

import io.silentsea.geomac.domain.repositories.GeomacRepository

class GetAllUseCase(private val geomacRepository: GeomacRepository) {
    suspend operator fun invoke() = geomacRepository.getAll()
}