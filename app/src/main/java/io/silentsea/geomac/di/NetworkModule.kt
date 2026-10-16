package io.silentsea.geomac.di

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.silentsea.geomac.BuildConfig
import io.silentsea.geomac.data.remote.AppleProvider
import io.silentsea.geomac.data.remote.GoogleProvider
import io.silentsea.geomac.data.remote.LocationProvider
import io.silentsea.geomac.data.remote.MicrosoftProvider
import io.silentsea.geomac.data.remote.MylnikovProvider
import io.silentsea.geomac.data.remote.YandexProvider
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient(CIO) {
            followRedirects = false
            expectSuccess = true

            install(HttpRedirect) {
                allowHttpsDowngrade = true
                checkHttpMethod = false
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 20_000
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d("geomac", message)
                        }
                    }
                    level = LogLevel.ALL
                }
            }
        }
    }

    single<List<LocationProvider>> {
        listOf(
            AppleProvider(get()),
            GoogleProvider(get()),
            MicrosoftProvider(get()),
            MylnikovProvider(get()),
            YandexProvider(get())
        )
    }
}
