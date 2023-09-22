package no.nav.sosialhjelp.plugins

import com.apurebase.kgraphql.GraphQL
import io.ktor.client.HttpClient
import io.ktor.server.application.*
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import no.nav.sosialhjelp.external.FiksClient
import no.nav.sosialhjelp.external.GeodataClient
import no.nav.sosialhjelp.graphql.kommuneSchema
import no.nav.sosialhjelp.maskinporten.Oauth2JwtProvider
import no.nav.sosialhjelp.utils.Config
import no.nav.sosialhjelp.utils.Env

fun Application.configureGraphQL(maskinportenClient: Oauth2JwtProvider, client: HttpClient) {
  install(GraphQL) {
    useDefaultPrettyPrinter = true
    playground = true

    if (Config.env != Env.LOCAL) {
      wrap { authenticate(optional = true, build = it, configurations = arrayOf("azuread")) }
    }

    context { call ->
      call.authentication.principal<JWTPrincipal>()?.let { +it }
      +FiksClient(maskinportenClient, client)
      +GeodataClient(client)
      +log
    }

    schema { kommuneSchema() }
  }
}
