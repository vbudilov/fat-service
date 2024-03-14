val ktor_version = "2.3.3" //: String by project
val kotlin_version = "1.9.0" //: String by project
val logback_version = "1.2.3" //: String by project

plugins {
    kotlin("jvm") version "1.9.0"
    id("io.ktor.plugin") version "2.3.3"
//    kotlin("plugin.serialization") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

group = "com.budilov.fat"
version = "0.0.1"
val aws_v2_sdk_version = "2.25.7"
val klaxonVersion = "5.5"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-rate-limit")

    // Auth
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")

    implementation("io.ktor:ktor-server-auto-head-response-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-swagger-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")

    // Response serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml:2.3.3")

    // AWS
    implementation("software.amazon.awssdk:s3:$aws_v2_sdk_version")
    implementation("software.amazon.awssdk:ssm:$aws_v2_sdk_version")
    implementation("software.amazon.awssdk:dynamodb:${aws_v2_sdk_version}")
    implementation("software.amazon.awssdk:bedrockruntime:${aws_v2_sdk_version}")
    implementation("software.amazon.awssdk:sqs:${aws_v2_sdk_version}")

    // Needed for S3
    implementation("commons-io:commons-io:2.11.0")

    // Json marshalling
    implementation("com.beust:klaxon:$klaxonVersion")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks {
    register("fatJar", Jar::class.java) {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes("Main-Class" to "com.budilov.fat.ApplicationKt")
        }
        from(configurations.runtimeClasspath.get()
            .onEach { println("add from dependencies: ${it.name}") }
            .map { if (it.isDirectory) it else zipTree(it) })

        val sourcesMain = sourceSets.main.get()
        sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
        from(sourcesMain.output)

    }
}
