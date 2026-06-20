package io.silentsea.geomac.di

import io.silentsea.geomac.domain.usecases.DeleteUseCase
import io.silentsea.geomac.domain.usecases.GetAllInRangesUseCase
import io.silentsea.geomac.domain.usecases.GetAllUseCase
import io.silentsea.geomac.domain.usecases.SearchUseCase
import io.silentsea.geomac.domain.usecases.UndoUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val useCasesModule = module {
    singleOf(::SearchUseCase)
    singleOf(::DeleteUseCase)
    singleOf(::UndoUseCase)
    singleOf(::GetAllUseCase)
    singleOf(::GetAllInRangesUseCase)
}