package no.nav.sosialhjelp.graphql

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.apurebase.kgraphql.schema.execution.Executor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.util.logging.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nidomiro.kdataloader.ExecutionResult
import no.nav.sosialhjelp.external.FiksClient
import no.nav.sosialhjelp.external.GeodataClient
import no.nav.sosialhjelp.manueltpakoblet.getManuellKommune
import no.nav.sosialhjelp.manueltpakoblet.getManuelleKommuner

fun SchemaBuilder.kommuneSchema() {

  configure { executor = Executor.DataLoaderPrepared }

  query("kommuner") {
    description = "Alle kommuner"
    resolver { context: Context ->
      context.get<Logger>()!!.info("Henter alle kommuner fra fiks")
      val fiksClient = context.get<FiksClient>()!!
      val fiksKommuner =
          fiksClient.getAllFiksKommuner().map { fiksKommune ->
            Kommune(
                harMidlertidigDeaktivertMottak = fiksKommune.harMidlertidigDeaktivertMottak,
                harMidlertidigDeaktivertOppdateringer =
                    fiksKommune.harMidlertidigDeaktivertOppdateringer,
                harNksTilgang = fiksKommune.harNksTilgang,
                kanMottaSoknader = fiksKommune.kanMottaSoknader,
                kanOppdatereStatus = fiksKommune.kanOppdatereStatus,
                kommunenummer = fiksKommune.kommunenummer,
                kontaktpersoner =
                    Kontaktpersoner(
                        fiksKommune.kontaktpersoner.fagansvarligEpost,
                        fiksKommune.kontaktpersoner.tekniskAnsvarligEpost),
            )
          }
      val alleKommuner =
          fiksKommuner
              .plus(
                  getManuelleKommuner().map {
                    Kommune(kommunenummer = it, kanMottaSoknader = true, kanOppdatereStatus = false)
                  })
              .distinctBy { it.kommunenummer }

      val kommunerKunManuelle =
          alleKommuner
              .filterNot {
                it.kommunenummer in fiksKommuner.map { fiksKommune -> fiksKommune.kommunenummer }
              }
              .map { it.kommunenummer }
      context.get<Logger>()!!.info("Kommuner som kun er manuelt p??koblet: $kommunerKunManuelle")

      alleKommuner
    }
  }

  query("kommune") {
    description = "En enkelt kommune"
    resolver { kommunenummer: String, context: Context ->
      val fiksClient = context.get<FiksClient>()!!
      val log = context.get<Logger>()!!
      fiksClient.getFiksKommune(kommunenummer)?.let {
        Kommune(
            it.harMidlertidigDeaktivertMottak,
            it.harMidlertidigDeaktivertOppdateringer,
            it.harNksTilgang,
            it.kanMottaSoknader,
            it.kanOppdatereStatus,
            it.kommunenummer,
            Kontaktpersoner(
                it.kontaktpersoner.fagansvarligEpost, it.kontaktpersoner.tekniskAnsvarligEpost))
      }
          ?: getManuellKommune(kommunenummer)?.let {
            log.info("Fant manuelt p??koblet kommune: $kommunenummer")
            Kommune(kommunenummer = it, kanMottaSoknader = true)
          }
    }
  }

  query("kommuneSearch") {
    description = "S??k etter kommune"
    resolver { searchString: String, context: Context ->
      context.get<GeodataClient>()!!.search(searchString).kommuner.map {
        KommuneSearchResult(
            fylkesnavn = it.fylkesnavn ?: error("${it.kommunenavnNorsk} er uten fylke"),
            kommunenummer = it.kommunenummer,
            kommunenavn = it.kommunenavnNorsk)
      }
    }
  }
  type<Kommune> {
    dataProperty("kommunenavn") {
      prepare { kommune -> kommune.kommunenummer }
      loader { ids ->
        // Man f??r ikke context i data loadern, s?? m?? konstruere egen klient her
        val geodataClient = GeodataClient(client)
        val kommuner =
            geodataClient.getAllGeodataKommuner().associate {
              it.kommunenummer to it.kommunenavnNorsk
            }
        ids.map { ExecutionResult.Success(kommuner[it] ?: "Ukjent") }
      }
    }

    property("kontaktpersoner") {
      description = "Informasjon om kontaktpersoner i kommunen"
      resolver { kommune -> kommune.kontaktpersoner }
      accessRule { _, context: Context ->
        context.get<Logger>()!!.info("Kj??rer tilgangskontroll p?? path 'kontaktpersoner'")
        if (context.get<JWTPrincipal>() == null) UnauthorizedException() else null
      }
    }
  }
}

private val client = HttpClient {
  install(Logging) {
    logger = io.ktor.client.plugins.logging.Logger.DEFAULT
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

class UnauthorizedException : RuntimeException()

@Serializable
data class Kommune(
    val harMidlertidigDeaktivertMottak: Boolean = false,
    val harMidlertidigDeaktivertOppdateringer: Boolean = false,
    val harNksTilgang: Boolean = false,
    val kanMottaSoknader: Boolean = false,
    val kanOppdatereStatus: Boolean = false,
    val kommunenummer: String = "0301",
    val kontaktpersoner: Kontaktpersoner = Kontaktpersoner(),
    val kommunenavn: String = "Oslo"
)

@Serializable
data class Kontaktpersoner(
    val fagansvarligEpost: List<String> = emptyList(),
    val tekniskAnsvarligEpost: List<String> = emptyList()
)

@Serializable
data class KommuneSearchResult(
    val fylkesnavn: String,
    val kommunenummer: String,
    val kommunenavn: String
)
