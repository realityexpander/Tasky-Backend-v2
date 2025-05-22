@file:Suppress("PropertyName")
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val mongodb_version: String by project
val logback_version: String by project
val commons_codec_version: String by project
val koin_version: String by project


plugins {
    // Ktor core version. **NOTE**: Should be the same as `ktor_version` from `gradle.properties`
    id("io.ktor.plugin") version "3.1.3" // Breaking changes - Not compatible with Koin 4.0.0 yet

    // Ktor serialization version. **NOTE**: Should be the same as `kotlin_version` from `gradle.properties`
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"

    // Kotlin jvm version. **NOTE**: Should be same as `kotlin_version` from `gradle.properties`
    kotlin("jvm") version "2.1.21"

    id("com.github.ben-manes.versions") version "0.51.0"  // For listing dependency updates, run: ./gradlew dependencyUpdates
}

buildscript {
    dependencies {
        // AWS Beanstalk plugin for deploying to AWS Elastic Beanstalk
        classpath("gradle.plugin.fi.evident.gradle.beanstalk:gradle-beanstalk-plugin:0.3.3")
    }
}

group = "com.realityexpander"
version = "2.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

// add resources to the JAR


tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
		 jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Ktor dependencies
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")

    // S3 (AWS SDK) for uploading images
    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.783"))
    implementation("com.amazonaws:aws-java-sdk-s3")
//    implementation(platform("software.amazon.awssdk:sdk-core:2.17.205"))  // Breaking API Changes, Requires update to Gradle 8.13
//    implementation("software.amazon.awssdk:s3:2.17.205") // Breaking API Changes, Requires update to Gradle 8.13

    // Rate Limiting (CDA FIX - Upgrade to use built-in Ktor rate limiting)
    implementation("dev.forst:ktor-rate-limiting:2.2.4")

    // MongoDB
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:$mongodb_version")

    // BASE64 encoding/decoding
    implementation("commons-codec:commons-codec:$commons_codec_version")
    implementation("commons-validator:commons-validator:1.9.0")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-core:$koin_version")

    // Loading .conf files
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.6.0")

    // Testing
    testImplementation("io.insert-koin:koin-test:$koin_version")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
    testImplementation("io.mockk:mockk:1.14.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")

//    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version") // Leave for reference when 3.X.X is fixed
//    testImplementation("io.ktor:ktor-server-tests-jvm:3.0.0-beta-2") // Introduces breaking changes
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.13") // should be $ktor_version, repo is lagging...


}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get()
        )
    }
}
