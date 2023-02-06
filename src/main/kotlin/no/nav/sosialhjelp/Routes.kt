package no.nav.sosialhjelp

import io.ktor.client.HttpClient
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import no.nav.sosialhjelp.fiks.FiksKommuneResponse
import no.nav.sosialhjelp.fiks.KartverketKommune
import no.nav.sosialhjelp.fiks.getAllFiksKommuner
import no.nav.sosialhjelp.fiks.getAllGeodataKommuner
import no.nav.sosialhjelp.fiks.getFiksKommune
import no.nav.sosialhjelp.fiks.getGeodataKommune
import no.nav.sosialhjelp.plugins.TokenValidationContextPrincipal

fun Routing.naisRoutes(scrape: () -> String) {
  get("/internal/is_alive") { call.respondText("I'm alive!") }
  get("/internal/is_ready") { call.respondText("I'm ready!") }
  get("/internal/prometheus") { call.respond(scrape()) }
}

fun Routing.kommuner() {
  val httpClient = HttpClient()
  authenticate {
    get<Autorisert.Kommuner> {

    }

    get<Autorisert.Kommuner.Kommune> {

    }

    get<Kommuner> { kommuner ->
      val fiksKommunerDeferred = async { kommuner.getAllFiksKommuner(httpClient) }
      val geodataKommunerDeferred = async {
        kommuner.getAllGeodataKommuner(httpClient).associateBy { it.kommunenummer }
      }
      val fiksKommuner = fiksKommunerDeferred.await()
      val geodataKommuner = geodataKommunerDeferred.await()
      call.respond(
          fiksKommuner.map {
            it.mergeWith(
                geodataKommuner[it.kommunenummer],
                isAuthed = context.principal<TokenValidationContextPrincipal>() != null)
          })
    }

    get<Kommuner.Kommune> { (kommuner, kommunenummer) ->
      val fiksKommuneDeferred = async { kommuner.getFiksKommune(kommunenummer, httpClient) }
      val geodataKommuneDeferred = async { kommuner.getGeodataKommune(kommunenummer, httpClient) }
      val fiksKommune = fiksKommuneDeferred.await()
      val geodataKommune = geodataKommuneDeferred.await()
      call.respond(
          fiksKommune.mergeWith(
              geodataKommune,
              isAuthed = context.principal<TokenValidationContextPrincipal>() != null))
    }
  }
}

fun FiksKommuneResponse.mergeWith(geodataKommune: KartverketKommune?, isAuthed: Boolean) =
    KommuneResponse(
        harMidlertidigDeaktivertMottak = harMidlertidigDeaktivertMottak,
        harMidlertidigDeaktivertOppdateringer = harMidlertidigDeaktivertOppdateringer,
        harNksTilgang = harNksTilgang,
        kanMottaSoknader = kanMottaSoknader,
        kanOppdatereStatus = kanOppdatereStatus,
        kommunenummer = kommunenummer,
        kommunenavn = geodataKommune?.kommunenavnNorsk ?: "Ukjent kommune",
        kontaktpersoner =
            if (isAuthed) {
              Kontaktpersoner(
                  kontaktpersoner.fagansvarligEpost, kontaktpersoner.tekniskAnsvarligEpost)
            } else null)


@Resource("/kommuner")
class Kommuner {

  @Resource("{kommunenummer}")
  data class Kommune(val parent: Kommuner = Kommuner(), val kommunenummer: String)
}

@Resource("/autorisert")
class Autorisert {

  @Resource("/kommuner")
  class Kommuner {

    @Resource("{kommunenummer}")
    data class Kommune(val parent: Kommuner = Kommuner(), val kommunenummer: String)
  }
}
