package no.nav.sosialhjelp.plugins

import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting(scrape: () -> String) {
  routing {
    get("/internal/is_alive") { call.respondText("I'm alive!") }
    get("/internal/is_ready") { call.respondText("I'm ready!") }
    get("/internal/prometheus") { call.respond(scrape()) }
  }
}
