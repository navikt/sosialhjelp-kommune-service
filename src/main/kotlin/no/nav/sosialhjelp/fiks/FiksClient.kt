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
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlin.RuntimeException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.nav.sosialhjelp.maskinporten.HttpClientMaskinportenTokenProvider
import no.nav.sosialhjelp.utils.Environment
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Fiks-client")

private val httpClient = HttpClient {
  install(Logging) {
    logger = Logger.DEFAULT
    level = LogLevel.INFO
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

suspend fun getAllFiksKommuner(
    maskinportenClient: HttpClientMaskinportenTokenProvider
): List<FiksKommuneResponse> {
  val token = maskinportenClient.getToken()
  val request =
      httpClient.get("${Environment.Fiks.baseUrl}/digisos/api/v1/nav/kommuner") {
        header("IntegrasjonId", Environment.Fiks.integrasjonId)
        header("IntegrasjonPassord", Environment.Fiks.integrasjonPassord)
        header("Authorization", "Bearer $token")
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

suspend fun getFiksKommune(
    kommunenummer: String,
    maskinportenClient: HttpClientMaskinportenTokenProvider
): FiksKommuneResponse {
  val token = maskinportenClient.getToken()
  val request =
      httpClient.get("${Environment.Fiks.baseUrl}/digisos/api/v1/nav/kommuner/${kommunenummer}") {
        header("IntegrasjonId", Environment.Fiks.integrasjonId)
        header("IntegrasjonPassord", Environment.Fiks.integrasjonPassord)
        header("Authorization", "Bearer $token")
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
