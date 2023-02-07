package no.nav.sosialhjelp.utils
object Environment {
  val fiksBaseUrl: String =
      getEnvVar("FIKS_BASE_URL", "http://localhost:8989/sosialhjelp/mock-alt-api/fiks")
  val geodataBaseUrl: String = getEnvVar("GEODATA_BASE_URL", "https://ws.geonorge.no")
  val env: Env = Env.valueOf(getEnvVar("ENV", "MOCK_ALT"))
}

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName)
        ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

enum class Env {
  MOCK_ALT,
  TEST,
  PROD
}
