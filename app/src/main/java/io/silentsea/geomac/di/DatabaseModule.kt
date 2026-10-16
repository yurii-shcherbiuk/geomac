package io.silentsea.geomac.di

import androidx.room3.Room
import io.silentsea.geomac.data.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "geomac-db").build()
    }

    single { get<AppDatabase>().geomacDao() }
}
