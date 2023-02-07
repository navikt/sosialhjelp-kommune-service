package no.nav.sosialhjelp

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.sosialhjelp.plugins.configureGraphQL
import no.nav.sosialhjelp.plugins.configureHTTP
import no.nav.sosialhjelp.plugins.configureMonitoring
import no.nav.sosialhjelp.plugins.configureRouting
import no.nav.sosialhjelp.plugins.configureSecurity
import no.nav.sosialhjelp.plugins.configureSerialization

fun main() {
  embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
      .start(wait = true)
}

fun Application.module() {
  val micrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
  install(MicrometerMetrics) { registry = micrometerRegistry }
  configureRouting(micrometerRegistry::scrape)
  configureSecurity()
  configureHTTP()
  configureMonitoring()
  configureSerialization()
  configureGraphQL()
}
