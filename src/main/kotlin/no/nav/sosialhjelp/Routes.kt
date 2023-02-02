package no.nav.sosialhjelp

import io.ktor.client.HttpClient
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import no.nav.sosialhjelp.fiks.FiksKommuneResponse
import no.nav.sosialhjelp.fiks.KartverketKommune
import no.nav.sosialhjelp.fiks.getFiksKommune
import no.nav.sosialhjelp.fiks.getGeodataKommune

fun Routing.naisRoutes(scrape: () -> String) {
  get("/internal/is_alive") { call.respondText("I'm alive!") }
  get("/internal/is_ready") { call.respondText("I'm ready!") }
  get("/internal/prometheus") { call.respond(scrape()) }
}

fun Routing.kommuner() {
  val httpClient = HttpClient()
  get<Kommuner> { TODO() }
  get<Kommuner.Kommune> { (_, kommunenummer) ->
    val fiksKommuneDeferred = async { getFiksKommune(kommunenummer, httpClient) }
    val geodataKommuneDeferred = async { getGeodataKommune(kommunenummer, httpClient) }
    val fiksKommune = fiksKommuneDeferred.await()
    val geodataKommune = geodataKommuneDeferred.await()
    call.respond(fiksKommune.mergeWith(geodataKommune))
  }
}

fun FiksKommuneResponse.mergeWith(geodataKommune: KartverketKommune) =
    KommuneResponse(
        harMidlertidigDeaktivertMottak = harMidlertidigDeaktivertMottak,
        harMidlertidigDeaktivertOppdateringer = harMidlertidigDeaktivertOppdateringer,
        harNksTilgang = harNksTilgang,
        kanMottaSoknader = kanMottaSoknader,
        kanOppdatereStatus = kanOppdatereStatus,
        kommunenummer = kommunenummer,
        kommunenavn = geodataKommune.kommunenavnNorsk)

@Serializable
data class KommuneResponse(
    val harMidlertidigDeaktivertMottak: Boolean,
    val harMidlertidigDeaktivertOppdateringer: Boolean,
    val harNksTilgang: Boolean,
    val kanMottaSoknader: Boolean,
    val kanOppdatereStatus: Boolean,
    val kommunenummer: String,
    val kommunenavn: String,
)

@Resource("/kommuner")
class Kommuner {

  @Resource("{kommunenummer}")
  data class Kommune(val parent: Kommuner = Kommuner(), val kommunenummer: String) {

    @Resource("/kontaktpersoner") data class Kontaktpersoner(val parent: Kommune)
  }
}
