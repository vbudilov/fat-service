package com.budilov.fat.services

import kotlinx.coroutines.future.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class AnthropicService {

    private val modelId = "anthropic.claude-3-sonnet-20240229-v1:0"
    private val client = BedrockRuntimeAsyncClient.builder()
//        .credentialsProvider(
//            ProfileCredentialsProvider.builder()
//                .profileName("thepartials")
//                .build()
//        )
        .region(software.amazon.awssdk.regions.Region.US_EAST_1).build()

    suspend fun invokeClaude3WithText(prompt: String) {

        try {
            val requestBody = Json.encodeToString(buildJsonObject {
                put("anthropic_version", "bedrock-2023-05-31")
                put("max_tokens", 1024)
                putJsonArray("messages") {
                    addJsonObject {
                        put("role", "user")
                        putJsonArray("content") {
                            addJsonObject {
                                put("type", "text")
                                put("text", prompt)
                            }
                        }
                    }
                }
            })

            val response = client.invokeModel(
                InvokeModelRequest.builder().modelId(modelId).body(SdkBytes.fromUtf8String(requestBody)).build()
            ).await()

            val responseBody = Json.parseToJsonElement(response.body().asUtf8String())
            val inputTokens = responseBody.jsonObject["usage"]?.jsonObject?.get("input_tokens")?.jsonPrimitive?.int
            val outputTokens = responseBody.jsonObject["usage"]?.jsonObject?.get("output_tokens")?.jsonPrimitive?.int
            val outputList = responseBody.jsonObject["content"]?.jsonArray

            println("Invocation details:")
            println("- The input length is $inputTokens tokens.")
            println("- The output length is $outputTokens tokens.")

            println("- The model returned ${outputList?.size ?: 0} response(s):")
            outputList?.forEach { output ->
                println(output.jsonObject["text"]?.jsonPrimitive?.content)
            }
        } catch (e: Exception) {
            println("Couldn't invoke Claude 3 Sonnet. Here's why: ${e.message}")
            throw e
        }
    }

    suspend fun invokeClaude3Multimodal(prompt: String, base64ImageData: String): String {

        var claudeResponse = ""

        val requestBody = buildJsonObject {
            put("anthropic_version", "bedrock-2023-05-31")
            put("max_tokens", 2048)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "text")
                            put("text", prompt)
                        }
                        addJsonObject {
                            put("type", "image")
                            putJsonObject("source") {
                                put("type", "base64")
                                put("media_type", "image/png")
                                put("data", base64ImageData)
                            }
                        }
                    }
                }
            }
        }.toString()

        try {
            val response = client.invokeModel(
                InvokeModelRequest.builder().modelId(modelId).body(SdkBytes.fromString(requestBody, Charsets.UTF_8))
                    .build()
            ).await()

            val result = Json.parseToJsonElement(response.body().asUtf8String())
            val inputTokens = result.jsonObject["usage"]?.jsonObject?.get("input_tokens")?.jsonPrimitive?.int
            val outputTokens = result.jsonObject["usage"]?.jsonObject?.get("output_tokens")?.jsonPrimitive?.int
            val outputList = result.jsonObject["content"]?.jsonArray

            println("Invocation details:")
            println("- The input length is $inputTokens tokens.")
            println("- The output length is $outputTokens tokens.")

            outputList?.forEach { output ->
                claudeResponse += output.jsonObject["text"]?.jsonPrimitive?.content
                println(output.jsonObject["text"]?.jsonPrimitive?.content)
            }
        } catch (e: Exception) {
            println("Couldn't invoke Claude 3 Sonnet. Here's why: ${e.message}")
            throw e
        }

        return claudeResponse
    }

    fun encodeImageToBase64(imagePath: String): String {
        // Read the image file to byte array
        val bytes = Files.readAllBytes(Paths.get(imagePath))

        // Encode the byte array to Base64 string
        return Base64.getEncoder().encodeToString(bytes)
    }

}

suspend fun main(args: Array<String>) {
    val anthropicService = AnthropicService()

    val maxWords = 100
    val prompt = "You're describing this image for the blind...please keep the description at $maxWords"
//    AnthropicService.invokeClaude3WithText(prompt)

    val imagePath = "/Users/vladimir/Downloads/western_wall_israel.jpeg" // Replace with your image path
    val base64Image = anthropicService.encodeImageToBase64(imagePath)

    anthropicService.invokeClaude3Multimodal(prompt, base64Image)
    //AnthropicService.invokeClaude3WithText("The quick brown fox jumps over the lazy dog")
    //AnthropicService.invokeClaude3WithText("The quick brown fox jumps over the lazy dog.")
    //AnthropicService.invokeClaude3WithText("The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog.")
    //AnthropicService.invokeClaude3WithText("The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog.")
    //AnthropicService.invokeClaude3WithText("The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog.")
}
