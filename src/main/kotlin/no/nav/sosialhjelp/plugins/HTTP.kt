package no.nav.sosialhjelp.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import no.nav.sosialhjelp.UnauthorizedException

fun Application.configureHTTP() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      this@configureHTTP.log.error("Error on ${call.request.path()}", cause)
      when (cause) {
        is UnauthorizedException ->
            call.respondText(text = "401: $cause", status = HttpStatusCode.Unauthorized)
      }
      call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
    }
  }
}
