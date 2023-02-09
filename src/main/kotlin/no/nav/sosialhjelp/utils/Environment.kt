package no.nav.sosialhjelp.utils

object Environment {
  val fiksBaseUrl: String =
      getEnvVar("FIKS_BASE_URL", "http://localhost:8989/sosialhjelp/mock-alt-api/fiks")
  val geodataBaseUrl: String = getEnvVar("GEODATA_BASE_URL", "https://ws.geonorge.no")
  val env: Env = Env.valueOf(getEnvVar("ENV", "MOCK_ALT"))

  object Maskinporten {
    val wellKnownUrl: String = getEnvVar("MASKINPORTEN_WELL_KNOWN_URL")
    val clientId: String = getEnvVar("MASKINPORTEN_CLIENTID", "maskinporten_clientid")
    val clientJwk: String = getEnvVar("MASKINPORTEN_CLIENT_JWK", "generateRSA")
    val scopes: String = getEnvVar("MASKINPORTEN_SCOPES", "scopes")
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
