package no.nav.sosialhjelp

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.apurebase.kgraphql.schema.execution.Executor
import io.ktor.client.HttpClient
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.util.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import nidomiro.kdataloader.ExecutionResult
import no.nav.sosialhjelp.fiks.getAllFiksKommuner
import no.nav.sosialhjelp.fiks.getAllGeodataKommuner
import no.nav.sosialhjelp.fiks.getFiksKommune
import no.nav.sosialhjelp.maskinporten.Oauth2JwtProvider

fun SchemaBuilder.kommuneSchema(maskinportenClient: Oauth2JwtProvider, client: HttpClient) {

  configure { executor = Executor.DataLoaderPrepared }

  query("kommuner") {
    description = "Alle kommuner"
    resolver { context: Context ->
      context.get<Logger>()!!.info("Henter alle kommuner fra fiks")
      getAllFiksKommuner(maskinportenClient, client).map { fiksKommune ->
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
    }
  }

  query("kommune") {
    description = "En enkelt kommune"
    resolver { kommunenummer: String ->
      withContext(Dispatchers.IO) {
        getFiksKommune(kommunenummer, maskinportenClient, client).let {
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
      }
    }
  }

  type<Kommune> {
    dataProperty("kommunenavn") {
      prepare { kommune -> kommune.kommunenummer }
      loader { ids ->
        val kommuner =
            getAllGeodataKommuner(client).associate { it.kommunenummer to it.kommunenavnNorsk }
        ids.map { ExecutionResult.Success(kommuner[it] ?: "Ukjent") }
      }
    }

    property("kontaktpersoner") {
      description = "Informasjon om kontaktpersoner i kommunen"
      resolver { kommune -> kommune.kontaktpersoner }
      accessRule { _, context: Context ->
        context.get<Logger>()!!.info("Kjører tilgangskontroll på path 'kontaktpersoner'")
        if (context.get<JWTPrincipal>() == null) UnauthorizedException() else null
      }
    }
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
