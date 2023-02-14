package no.nav.sosialhjelp.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.*

fun Application.configureMonitoring() {
  install(CallLogging) {
    level = Level.DEBUG
    filter { call -> call.request.path().startsWith("/graphql") }
    callIdMdc("call-id")
  }
  install(CallId) {
    header(HttpHeaders.XRequestId)
    verify { callId: String -> callId.isNotEmpty() }
  }
}
