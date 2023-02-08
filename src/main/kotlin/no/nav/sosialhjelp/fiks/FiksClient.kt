package no.nav.sosialhjelp.fiks

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlin.RuntimeException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.nav.sosialhjelp.utils.Env
import no.nav.sosialhjelp.utils.Environment
import no.nav.sosialhjelp.utils.getEnvVar
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Fiks-client")

val httpClient = HttpClient {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
    }
}

fun maskinporten(): String {
    return ""
}


suspend fun getAllFiksKommuner(): List<FiksKommuneResponse> {
  if (Environment.env == Env.PROD) {
    val response =
        httpClient
            .post(getEnvVar("maskinporten_well_known_url")) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody()
            }
            .body(BodyInserters.fromFormData(params))
            .retrieve()
            .bodyToMono<MaskinportenResponse>()
            .doOnSuccess { log.info("Hentet token fra Maskinporten") }
            .doOnError { log.warn("Noe feilet ved henting av token fra Maskinporten", it) }
            .block()
            ?: throw RuntimeException("Noe feilet ved henting av token fra Maskinporten")
  }

  val request =
      httpClient.get("${Environment.fiksBaseUrl}/digisos/api/v1/nav/kommuner") {
        header("Authorization", "Bearer abc")
        accept(ContentType.Application.Json)
      }
  when (request.status.value) {
    in 200..299 -> {
      logger.info("Fikk svar på /kommmuner fra Fiks.")
      return request.body()
    }
    in 400..499 -> {
      logger.error("4xx fra Fiks på /kommuner")
      throw RuntimeException("feeekk")
    }
    else -> {
      logger.error("5xx fra Fiks på /kommuner")
      throw RuntimeException("feeek")
    }
  }
}

suspend fun getAllGeodataKommuner(): List<KartverketKommune> {
  val request =
      httpClient.get("${Environment.geodataBaseUrl}/kommuneinfo/v1/kommuner") {
        accept(ContentType.Application.Json)
      }
  return when (request.status.value) {
    in 200..299 -> {
      logger.info("Fikk svar fra geodata")
      request.body()
    }
    in 400..499 -> {
      logger.error("4xx fra geodata")
      throw RuntimeException("feekk")
    }
    else -> {
      logger.error("5xx fra geodata")
      throw RuntimeException("feeeeeeeeeeeeeeeek")
    }
  }
}

suspend fun getFiksKommune(kommunenummer: String): FiksKommuneResponse {
  val request =
      httpClient.get("${Environment.fiksBaseUrl}/digisos/api/v1/nav/kommuner/${kommunenummer}") {
        header("Authorization", "Bearer abc")
        accept(ContentType.Application.Json)
      }
  when (request.status.value) {
    in 200..299 -> {
      logger.info("Fikk svar på /kommmuner/$kommunenummer fra Fiks.")
      return request.body()
    }
    in 400..499 -> {
      logger.error("4xx fra Fiks på /kommuner/$kommunenummer")
      throw RuntimeException("feeekk")
    }
    else -> {
      logger.error("5xx fra Fiks på /kommuner/$kommunenummer")
      throw RuntimeException("feeek")
    }
  }
}

suspend fun getGeodataKommune(kommunenummer: String): KartverketKommune {
  val request =
      httpClient.get("${Environment.geodataBaseUrl}/kommuneinfo/v1/kommuner/${kommunenummer}") {
        accept(ContentType.Application.Json)
      }
  return when (request.status.value) {
    in 200..299 -> {
      logger.info("Fikk svar fra geodata")
      request.body()
    }
    in 400..499 -> {
      logger.error("4xx fra geodata")
      throw RuntimeException("feekk")
    }
    else -> {
      logger.error("5xx fra geodata")
      throw RuntimeException("feeeeeeeeeeeeeeeek")
    }
  }
}

@Serializable
data class KartverketKommune(
    val kommunenavn: String,
    val kommunenavnNorsk: String,
    val kommunenummer: String,
)
