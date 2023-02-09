package no.nav.sosialhjelp

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.apurebase.kgraphql.schema.execution.Executor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import nidomiro.kdataloader.ExecutionResult
import no.nav.sosialhjelp.fiks.getAllFiksKommuner
import no.nav.sosialhjelp.fiks.getAllGeodataKommuner
import no.nav.sosialhjelp.fiks.getFiksKommune
import no.nav.sosialhjelp.fiks.getGeodataKommune
import no.nav.sosialhjelp.maskinporten.HttpClientMaskinportenTokenProvider
import no.nav.sosialhjelp.plugins.TokenValidationContextPrincipal
import no.nav.sosialhjelp.utils.Env
import no.nav.sosialhjelp.utils.Environment

fun SchemaBuilder.kommuneSchema(maskinportenClient: HttpClientMaskinportenTokenProvider) {

  configure { executor = Executor.DataLoaderPrepared }

  query("kommuner") {
    description = "Alle kommuner"
    resolver { ->
      getAllFiksKommuner(maskinportenClient).map { fiksKommune ->
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
        val kommune = async { getFiksKommune(kommunenummer, maskinportenClient) }
        val andreKommune = async { getGeodataKommune(kommunenummer) }
        with(kommune.await()) {
          Kommune(
                  harMidlertidigDeaktivertMottak,
                  harMidlertidigDeaktivertOppdateringer,
                  harNksTilgang,
                  kanMottaSoknader,
                  kanOppdatereStatus,
                  kommunenummer,
                  Kontaktpersoner(
                      kontaktpersoner.fagansvarligEpost, kontaktpersoner.tekniskAnsvarligEpost))
              .also { it.kommunenavn = andreKommune.await().kommunenavnNorsk }
        }
      }
    }
  }

  type<Kommune> {
    dataProperty("kommunenavn") {
      prepare { kommune -> kommune.kommunenummer }
      loader { ids ->
        val kommuner =
            getAllGeodataKommuner().associate { it.kommunenummer to it.kommunenavnNorsk }
        ids.map { ExecutionResult.Success(kommuner[it] ?: "Ukjent") }
      }
    }

    property("kontaktpersoner") {
      description = "Informasjon om kontaktpersoner i kommunen"
      resolver { kommune -> kommune.kontaktpersoner }
      accessRule { _, context: Context ->
        when {
          Environment.env != Env.PROD -> null
          context.get<TokenValidationContextPrincipal>() == null -> NoTokenException()
          context.get<TokenValidationContextPrincipal>()?.context?.hasValidToken() == false ->
              UnauthorizedException()
          else -> null
        }
      }
    }
  }
}

class NoTokenException : RuntimeException()

class UnauthorizedException : RuntimeException()

@Serializable
data class Kommune(
    val harMidlertidigDeaktivertMottak: Boolean,
    val harMidlertidigDeaktivertOppdateringer: Boolean,
    val harNksTilgang: Boolean,
    val kanMottaSoknader: Boolean,
    val kanOppdatereStatus: Boolean,
    val kommunenummer: String,
    val kontaktpersoner: Kontaktpersoner
) {
  lateinit var kommunenavn: String
}

@Serializable
data class Kontaktpersoner(
    val fagansvarligEpost: List<String>,
    val tekniskAnsvarligEpost: List<String>
)
