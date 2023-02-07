package no.nav.sosialhjelp.fiks

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import kotlin.RuntimeException
import kotlinx.serialization.Serializable
import no.nav.sosialhjelp.utils.Environment
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Fiks-client")

suspend fun getAllFiksKommuner(client: HttpClient): List<FiksKommuneResponse> {
  val request =
      client.get("${Environment.fiksBaseUrl}/digisos/api/v1/nav/kommuner") {
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

suspend fun getAllGeodataKommuner(client: HttpClient): List<KartverketKommune> {
  val request =
      client.get("${Environment.geodataBaseUrl}/kommuneinfo/v1/kommuner") {
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

suspend fun getFiksKommune(kommunenummer: String, client: HttpClient): FiksKommuneResponse {
  val request =
      client.get("${Environment.fiksBaseUrl}/digisos/api/v1/nav/kommuner/${kommunenummer}") {
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

suspend fun getGeodataKommune(kommunenummer: String, client: HttpClient): KartverketKommune =
    client.get("${Environment.geodataBaseUrl}/kommuneinfo/v1/kommune/${kommunenummer}").body()

@Serializable
data class KartverketKommune(
    val kommunenavn: String,
    val kommunenavnNorsk: String,
    val kommunenummer: String,
)
