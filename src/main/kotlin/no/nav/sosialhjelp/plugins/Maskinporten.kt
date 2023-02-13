package no.nav.sosialhjelp.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.nav.sosialhjelp.maskinporten.HttpClientMaskinportenTokenProvider
import no.nav.sosialhjelp.maskinporten.MaskinportenConfig
import no.nav.sosialhjelp.utils.Env
import no.nav.sosialhjelp.utils.Environment

fun Application.configureMaskinporten(): HttpClientMaskinportenTokenProvider {
  if (Environment.env == Env.TEST) {
    return HttpClientMaskinportenTokenProvider(
        MaskinportenConfig(tokenEndpointUrl = "token_url", issuer = "issuer"))
  }
  val client = HttpClient {
    install(ContentNegotiation) {
      json(
          Json {
            ignoreUnknownKeys = true
            isLenient = true
          })
    }
  }
  val wellKnown: WellKnown = runBlocking {
    client.get(Environment.Maskinporten.wellKnownUrl).body<WellKnown>().also {
      log.info("Hentet well known for Maskinporten")
    }
  }
  val config = MaskinportenConfig(wellKnown.token_endpoint, wellKnown.issuer)
  return HttpClientMaskinportenTokenProvider(config)
}

@Serializable
data class WellKnown(
    val issuer: String,
    val token_endpoint: String,
)
