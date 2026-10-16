package io.silentsea.geomac.di

import io.silentsea.geomac.domain.usecases.DeleteUseCase
import io.silentsea.geomac.domain.usecases.ExportUseCase
import io.silentsea.geomac.domain.usecases.GetAllInRangesUseCase
import io.silentsea.geomac.domain.usecases.ImportUseCase
import io.silentsea.geomac.domain.usecases.SearchUseCase
import io.silentsea.geomac.domain.usecases.UndoUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCasesModule = module {
    factoryOf(::SearchUseCase)
    factoryOf(::DeleteUseCase)
    factoryOf(::UndoUseCase)
    factoryOf(::GetAllInRangesUseCase)
    factoryOf(::ExportUseCase)
    factoryOf(::ImportUseCase)
}
