package io.silentsea.geomac.domain.usecases

import io.silentsea.geomac.domain.repositories.GeomacRepository

class DeleteUseCase(private val geomacRepository: GeomacRepository) {
    suspend operator fun invoke(mac: Long) = geomacRepository.delete(mac)
}
