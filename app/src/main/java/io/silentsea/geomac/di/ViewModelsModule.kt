package io.silentsea.geomac.di

import io.silentsea.geomac.ui.GeomacViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelsModule = module {
    viewModelOf(::GeomacViewModel)
}
