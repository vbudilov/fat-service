package com.budilov.fat.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun Application.configureSecurity() {

    val l: Logger = LoggerFactory.getLogger("configureSecurity")

    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtAudienceWeb = environment.config.property("jwt.audienceWeb").getString()

    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm

            val jwkProvider = JwkProviderBuilder(jwtIssuer)
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

            verifier(jwkProvider, jwtIssuer) {
                acceptLeeway(3)
            }

            validate { credential ->
                if (listOf(jwtAudience, jwtAudienceWeb).intersect(credential.payload.audience.toSet())
                        .isNotEmpty()
                ) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
