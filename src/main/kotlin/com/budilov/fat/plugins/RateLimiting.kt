package com.budilov.fat.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*

fun Application.configureRateLimiting() {
    install(RateLimit)
}
