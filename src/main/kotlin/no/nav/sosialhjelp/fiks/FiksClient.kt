package no.nav.sosialhjelp.fiks

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import kotlin.RuntimeException
import kotlinx.serialization.Serializable
import no.nav.sosialhjelp.maskinporten.Oauth2JwtProvider
import no.nav.sosialhjelp.utils.Config
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Fiks-client")

suspend fun getAllFiksKommuner(
    maskinportenClient: Oauth2JwtProvider,
    client: HttpClient
): List<FiksKommuneResponse> {
  val token = maskinportenClient.getToken()
  val request =
      client.get("${Config.Fiks.baseUrl}/digisos/api/v1/nav/kommuner") {
        header("IntegrasjonId", Config.Fiks.integrasjonId)
        header("IntegrasjonPassord", Config.Fiks.integrasjonPassord)
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

suspend fun getAllGeodataKommuner(client: HttpClient): List<KartverketKommune> {
  val request =
      client.get("${Config.geodataBaseUrl}/kommuneinfo/v1/kommuner") {
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
    maskinportenClient: Oauth2JwtProvider,
    client: HttpClient
): FiksKommuneResponse {
  val token = maskinportenClient.getToken()
  val request =
      client.get("${Config.Fiks.baseUrl}/digisos/api/v1/nav/kommuner/${kommunenummer}") {
        header("IntegrasjonId", Config.Fiks.integrasjonId)
        header("IntegrasjonPassord", Config.Fiks.integrasjonPassord)
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

suspend fun getGeodataKommune(kommunenummer: String, client: HttpClient): KartverketKommune {
  val request =
      client.get("${Config.geodataBaseUrl}/kommuneinfo/v1/kommuner/${kommunenummer}") {
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
