package no.nav.sosialhjelp.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.sosialhjelp.kommuner
import no.nav.sosialhjelp.naisRoutes

fun Application.configureRouting(scrape: () -> String) {
  routing {
    naisRoutes(scrape)
    kommuner()
  }
}
