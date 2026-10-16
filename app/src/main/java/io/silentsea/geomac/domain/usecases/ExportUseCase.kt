package io.silentsea.geomac.domain.usecases

import io.silentsea.geomac.domain.repositories.GeomacRepository
import kotlinx.io.RawSink

class ExportUseCase(private val geomacRepository: GeomacRepository) {
    suspend operator fun invoke(sink: RawSink) = geomacRepository.export(sink)
}
