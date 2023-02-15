package no.nav.sosialhjelp.plugins

import com.apurebase.kgraphql.GraphQL
import io.ktor.client.HttpClient
import io.ktor.server.application.*
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import no.nav.sosialhjelp.kommuneSchema
import no.nav.sosialhjelp.maskinporten.Oauth2JwtProvider

fun Application.configureGraphQL(maskinportenClient: Oauth2JwtProvider, client: HttpClient) {
  install(GraphQL) {
    useDefaultPrettyPrinter = true
    playground = true

    wrap { authenticate(optional = true, build = it, configurations = arrayOf("azuread")) }

    context { call ->
      call.authentication.principal<JWTPrincipal>()?.let { +it }
      +log
    }

    schema { kommuneSchema(maskinportenClient, client) }
  }
}
