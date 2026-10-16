package io.silentsea.geomac.di

import io.silentsea.geomac.data.repositories.GeomacRepositoryImpl
import io.silentsea.geomac.domain.repositories.GeomacRepository
import org.koin.dsl.module

val repositoriesModule = module {
    single<GeomacRepository> { GeomacRepositoryImpl(geomacDao = get(), providers = get()) }
}
