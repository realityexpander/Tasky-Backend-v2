package com.realityexpander

import com.realityexpander.di.configureKoin
import com.realityexpander.domain.security.token.TokenConfig
import com.realityexpander.domain.util.EnvironmentProvider
import com.realityexpander.plugins.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureKoin()
    val environmentVariables: EnvironmentProvider by inject()

    // Access Token Config for JWT & expiry
    val tokenConfig = TokenConfig(
        issuer = environmentVariables.jwtIssuer,
        audience = environmentVariables.jwtAudience,
        expiresIn = 10.seconds.inWholeMilliseconds,
        secret = environmentVariables.jwtSecret
    )

    configureSecurity(tokenConfig)
    configureRouting(tokenConfig)
    configureMonitoring()
    configureSerialization()
    configureRateLimiting()
}

// Fix for Koin injection for Koin 4.0.4, Ktor 3.1.3 in Routes (e.g. in Routing.kt)
inline fun <reified T : Any> Route.inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
) =
	lazy {
		GlobalContext.getKoinApplicationOrNull()?.koin?.
        get<T>(qualifier, parameters) ?:
         org.koin.java.KoinJavaComponent.inject<T>(T::class.java).value // uses org.koin.java.KoinJavaComponent.inject
	}


// Access global logger from Routes
val Route.log
    get() = application.log