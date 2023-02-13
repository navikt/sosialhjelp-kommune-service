package no.nav.sosialhjelp.utils

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import java.net.URL

object Environment {
  val geodataBaseUrl: String = getEnvVar("GEODATA_BASE_URL", "https://ws.geonorge.no")
  val env: Env = Env.valueOf(getEnvVar("ENV", "MOCK"))

  object Fiks {
    val baseUrl = getEnvVar("FIKS_BASE_URL", "http://localhost:8989/sosialhjelp/mock-alt-api/fiks")
    val integrasjonId = getEnvVar("INTEGRASJONSID_FIKS", "id")
    val integrasjonPassord = getEnvVar("INTEGRASJONPASSORD_FIKS", "passord")
  }

  object Maskinporten {
    val wellKnownUrl: String =
        getEnvVar(
            "MASKINPORTEN_WELL_KNOWN_URL",
            "http://localhost:8989/sosialhjelp/mock-alt-api/well-known/maskinporten")
    val clientId: String = getEnvVar("MASKINPORTEN_CLIENT_ID", "maskinporten_clientid")
    val clientJwk: String = getEnvVar("MASKINPORTEN_CLIENT_JWK", "generateRSA")
    val scopes: String = getEnvVar("MASKINPORTEN_SCOPES", "scopes")
  }

  object AzureADConfig {
    val clientId = getEnvVar("AZURE_APP_CLIENT_ID", "client_id")
    val issuer = getEnvVar("AZURE_OPENID_CONFIG_ISSUER", "issuer")
    val jwkProvider: JwkProvider =
        JwkProviderBuilder(
                URL(
                    getEnvVar(
                        "AZURE_OPENID_CONFIG_JWKS_URI",
                        "http://localhost:8989/sosialhjelp/mock-alt-api/jwks/azuread")))
            .build()
  }
}

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName)
        ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

enum class Env {
  MOCK,
  DEV,
  TEST,
  PROD
}
