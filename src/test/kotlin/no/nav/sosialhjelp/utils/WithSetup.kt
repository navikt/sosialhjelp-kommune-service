package no.nav.sosialhjelp.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import no.nav.sosialhjelp.maskinporten.Oauth2JwtProvider
import no.nav.sosialhjelp.plugins.configureGraphQL
import no.nav.sosialhjelp.plugins.configureHTTP
import no.nav.sosialhjelp.plugins.configureSecurity

class TestMaskinportenClient : Oauth2JwtProvider {
  override suspend fun getToken(): String = "abc"
}

val defaultApplicationBlock: Application.(client: HttpClient) -> Unit = { client ->
  configureSecurity()
  configureHTTP()
  configureGraphQL(TestMaskinportenClient(), client)
}

fun withSetup(
    authentication: Application.() -> Unit = Application::configureSecurity,
    block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
) = testApplication {
  val client = createClient {
    this@createClient.install(ContentNegotiation) {
      json(
          Json {
            isLenient = true
            ignoreUnknownKeys = true
          })
    }
  }
  application {
    authentication()
    configureHTTP()
    configureGraphQL(TestMaskinportenClient(), client)
  }
  this.block(client)
}
