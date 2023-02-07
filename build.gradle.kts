val ktor_version: String by project
val kotlin_version: String by project
val coroutines_version: String by project
val logback_version: String by project
val token_support_version: String by project
val prometheus_version: String by project
val kgraphql_version: String by project

plugins {
  kotlin("jvm") version "1.8.0"
  id("io.ktor.plugin") version "2.2.3"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

group = "no.nav.sosialhjelp"

version = "1.0.0"

application {
  mainClass.set("no.nav.sosialhjelp.ApplicationKt")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories { mavenCentral() }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "17" }

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")

  implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-call-id-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-openapi:$ktor_version")
  implementation("io.ktor:ktor-server-swagger:$ktor_version")
  implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-resources:$ktor_version")
  implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
  implementation("io.ktor:ktor-client-cio:$ktor_version")

  implementation("com.apurebase:kgraphql:$kgraphql_version")
  implementation("com.apurebase:kgraphql-ktor:$kgraphql_version")

  implementation("ch.qos.logback:logback-classic:$logback_version")

  implementation("no.nav.security:token-validation-core:${token_support_version}")

  implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")
    implementation("io.ktor:ktor-client-logging-jvm:2.2.3")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
