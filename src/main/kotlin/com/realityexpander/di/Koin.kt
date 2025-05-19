package com.realityexpander.di

import io.ktor.server.application.*
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.GlobalContext
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.koin

@OptIn(KoinInternalApi::class) // to access instanceRegistry
fun Application.configureKoin() {
    // Needed to prevent creating two Koin applications in tests
    if (GlobalContext.getKoinApplicationOrNull() == null) {
        install(Koin) {
            modules(appModule)
        }
    }

   // CDA Leave for debugging
//    if(GlobalContext.getKoinApplicationOrNull() != null) {
//         GlobalContext.get().instanceRegistry.instances.apply {
//             println("Instances: $this")
//         }
//    }
}