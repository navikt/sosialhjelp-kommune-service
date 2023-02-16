package no.nav.sosialhjelp.external

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import no.nav.sosialhjelp.maskinporten.Oauth2JwtProvider
import no.nav.sosialhjelp.utils.Config
import org.slf4j.LoggerFactory

class FiksClient(
    private val maskinportenClient: Oauth2JwtProvider,
    private val httpClient: HttpClient
) {
  private val log = LoggerFactory.getLogger(this::class.java)
  suspend fun getAllFiksKommuner(): List<FiksKommuneResponse> {
    val token = maskinportenClient.getToken()
    val response =
        httpClient.get("${Config.Fiks.baseUrl}/digisos/api/v1/nav/kommuner") {
          header("IntegrasjonId", Config.Fiks.integrasjonId)
          header("IntegrasjonPassord", Config.Fiks.integrasjonPassord)
          header("Authorization", "Bearer $token")
          accept(ContentType.Application.Json)
        }
    return Response<List<FiksKommuneResponse>>(response) {
          on2xx {
            log.info("Fikk ${response.status} fra Fiks")
            response.body()
          }
        }
        .respond()
  }

  suspend fun getFiksKommune(
      kommunenummer: String,
  ): FiksKommuneResponse? {
    val token = maskinportenClient.getToken()
    val response =
        httpClient.get("${Config.Fiks.baseUrl}/digisos/api/v1/nav/kommuner/${kommunenummer}") {
          header("IntegrasjonId", Config.Fiks.integrasjonId)
          header("IntegrasjonPassord", Config.Fiks.integrasjonPassord)
          header("Authorization", "Bearer $token")
          accept(ContentType.Application.Json)
        }
    return Response<FiksKommuneResponse>(response) {
          on2xx {
            logger.info("Fikk svar på /kommmuner/$kommunenummer fra Fiks.")
            it.body()
          }
        }
        .respondOrNull()
  }
}

class GeodataClient(private val httpClient: HttpClient) {
  private val log = LoggerFactory.getLogger(this::class.java)

  suspend fun getAllGeodataKommuner(): List<GeodataKommuneResponse> {
    val response =
        httpClient.get("${Config.geodataBaseUrl}/kommuneinfo/v1/kommuner") {
          accept(ContentType.Application.Json)
        }
    return Response<List<GeodataKommuneResponse>>(response) {
          on2xx {
            log.info("Fikk svar fra geodata")
            response.body()
          }
          on4xx {
            log.error("Fikk ${it.status} fra geodata")
            if (it.status == HttpStatusCode.NotFound) {
              emptyList()
            } else throw ClientRequestException(it, "Fikk feil fra geodata: ${it.status}")
          }
        }
        .respond()
  }
}
