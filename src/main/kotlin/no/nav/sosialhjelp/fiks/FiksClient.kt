package no.nav.sosialhjelp.fiks

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import java.lang.RuntimeException
import org.slf4j.LoggerFactory

private const val fiksBaseUrl = "https://api.fiks.ks.no"

val logger = LoggerFactory.getLogger("Fiks-client")

suspend fun getFiksKommune(kommunenummer: String, client: HttpClient): FiksKommuneResponse {
  val request =
      client.get("${fiksBaseUrl}/kommune/${kommunenummer}") {
        this.headers.append("Authorization", "Bearer abc")
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

suspend fun getGeodataKommune(kommunenummer: String, client: HttpClient): KartverketKommune =
    client.get("https://ws.geonorge.no/kommuneinfo/v1/kommune/${kommunenummer}").body()

data class KartverketKommune(
    val kommunenavn: String,
    val kommunenavnNorsk: String,
    val kommunenummer: String
)
