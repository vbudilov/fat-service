package com.budilov.fat.routes

import com.budilov.fat.plugins.CONTEXT_ROOT
import com.budilov.fat.services.AnthropicService
import com.budilov.fat.services.getRandomUUID
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File


fun Route.images() {
    val l: Logger = LoggerFactory.getLogger("images")
    val anthropicService = AnthropicService()

    route("$CONTEXT_ROOT/image") {

        post("/upload") {
//            val subId = call.principal<JWTPrincipal>()?.subject
//
//            l.info("subId=$subId message=Uploading a file")
//            l.info("subId: $subId")
l.info("in the upload method")
            // This uuid will be used for referencing the receipt

            val multipart = call.receiveMultipart()
            var fileName = getRandomUUID()
            val contentLength = call.request.header(HttpHeaders.ContentLength)

            val file = File("/tmp/$fileName")
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name == "file") {
                            fileName = part.originalFileName as String
                            val fileBytes = part.streamProvider().readBytes()
                            // Ensure the uploads directory exists
                            file.parentFile.mkdirs()

                            file.writeBytes(fileBytes)
//                            call.respondText("File uploaded successfully: $fileName")
                        }
                    }

                    else -> {}
                }
                part.dispose()
            }

            // Anthropic part
            l.info("maxWords")

            val maxWords = 100
            val prompt = "You're describing this image for the blind...please keep the description at $maxWords"
            val imagePath = file.path

            l.info("getting base64")
            l.info("imagePath: $imagePath")
            val base64Image = anthropicService.encodeImageToBase64(imagePath)
            l.info("calling anthropic")
            runBlocking {
                val claudeResponse = anthropicService.invokeClaude3Multimodal(prompt, base64Image)
                l.info("claudeResponse: $claudeResponse")

                call.respond(com.budilov.fat.ApiResponse(body = claudeResponse))
            }

        }

    }
}



