package io.silentsea.geomac

import android.app.Application
import io.silentsea.geomac.di.databaseModule
import io.silentsea.geomac.di.networkModule
import io.silentsea.geomac.di.repositoriesModule
import io.silentsea.geomac.di.useCasesModule
import io.silentsea.geomac.di.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GeomacApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(
                databaseModule,
                networkModule,
                repositoriesModule,
                useCasesModule,
                viewModelsModule
            )
        }
    }
}