package no.nav.sosialhjelp.plugins

import com.apurebase.kgraphql.GraphQL
import io.ktor.server.application.*
import io.ktor.server.application.Application
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import no.nav.security.token.support.v2.TokenValidationContextPrincipal
import no.nav.sosialhjelp.kommuneSchema
import no.nav.sosialhjelp.maskinporten.HttpClientMaskinportenTokenProvider
import no.nav.sosialhjelp.utils.Env
import no.nav.sosialhjelp.utils.Environment

fun Application.configureGraphQL(maskinportenClient: HttpClientMaskinportenTokenProvider) {
  install(GraphQL) {
    useDefaultPrettyPrinter = true
    playground = true

    wrap { authenticate(optional = true, build = it, configurations = arrayOf("azuread")) }

    context { call ->
      call.authentication.principal<JWTPrincipal>()?.let { +it }
      +log
    }

    schema { kommuneSchema(maskinportenClient) }
  }
}
