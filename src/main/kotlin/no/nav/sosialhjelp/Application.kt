package no.nav.sosialhjelp

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import no.nav.sosialhjelp.plugins.configureGraphQL
import no.nav.sosialhjelp.plugins.configureHTTP
import no.nav.sosialhjelp.plugins.configureMaskinporten
import no.nav.sosialhjelp.plugins.configureMonitoring
import no.nav.sosialhjelp.plugins.configureRouting
import no.nav.sosialhjelp.plugins.configureSecurity
import no.nav.sosialhjelp.plugins.configureSerialization

fun main() {
  embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
      .start(wait = true)
}

fun Application.module() {
  val client = HttpClient {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.INFO
    }
    install(ContentNegotiation) {
      json(
          Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
          })
    }
  }
  val micrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
  install(MicrometerMetrics) { registry = micrometerRegistry }
  configureRouting(micrometerRegistry::scrape)
  configureSecurity()
  configureHTTP()
  configureMonitoring()
  configureSerialization()
  val maskinportenClient = configureMaskinporten()
  configureGraphQL(maskinportenClient, client)
}
