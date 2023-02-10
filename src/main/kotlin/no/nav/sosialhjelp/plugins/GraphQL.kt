package no.nav.sosialhjelp.plugins

import com.apurebase.kgraphql.GraphQL
import io.ktor.server.application.*
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import no.nav.sosialhjelp.kommuneSchema
import no.nav.sosialhjelp.maskinporten.HttpClientMaskinportenTokenProvider
import no.nav.sosialhjelp.utils.Env
import no.nav.sosialhjelp.utils.Environment

fun Application.configureGraphQL(maskinportenClient: HttpClientMaskinportenTokenProvider) {
  install(GraphQL) {
    useDefaultPrettyPrinter = true
    playground = Environment.env != Env.PROD

//    wrap { authenticate(optional = true, build = it, configurations = arrayOf("tokenx")) }

    context { call ->
//      call.authentication.principal<TokenValidationContextPrincipal>()?.let { +it }
      +log
    }

    schema { kommuneSchema(maskinportenClient) }
  }
}
