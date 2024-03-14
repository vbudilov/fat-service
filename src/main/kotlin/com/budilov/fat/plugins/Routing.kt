package com.budilov.fat.plugins

import com.budilov.fat.routes.images
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val CONTEXT_ROOT = "/budilov/api/v1"

fun Application.configureRouting() {
    install(AutoHeadResponse)
    routing {
        authenticate("auth-jwt") {
            images()
        }

        get("/ping") {
            call.respondText("pong")
        }
    }
}
