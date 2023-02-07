package no.nav.sosialhjelp

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import no.nav.sosialhjelp.fiks.getAllFiksKommuner
import no.nav.sosialhjelp.fiks.getAllGeodataKommuner
import no.nav.sosialhjelp.plugins.TokenValidationContextPrincipal

fun SchemaBuilder.kommuneSchema() {
  val httpClient =
      HttpClient {
        install(Logging) {
          logger = Logger.DEFAULT
          level = LogLevel.HEADERS
        }
      }
  query("kommuner") {
    description = "Alle kommuner"
    resolver { ->
      withContext(Dispatchers.IO) {
        val fiksKommunerDeferred = async { getAllFiksKommuner(httpClient) }
        val geodataKommunerDeferred = async {
          getAllGeodataKommuner(httpClient).associateBy { it.kommunenummer }
        }
        val fiksKommuner = fiksKommunerDeferred.await()
        val geodataKommune = geodataKommunerDeferred.await()
        fiksKommuner.map { fiksKommune ->
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
              kommunenavn = geodataKommune[fiksKommune.kommunenummer]?.kommunenavnNorsk
                      ?: "Ukjent navn på kommune")
        }
      }
    }

    type<Kommune> {
      property<Kontaktpersoner>("kontaktpersoner") {
        description = "Informasjon om kontaktpersoner i kommunen"
        resolver { k -> k.kontaktpersoner }
        accessRule { _, context: Context ->
          when {
            context.get<TokenValidationContextPrincipal>() == null -> NoTokenException()
            context.get<TokenValidationContextPrincipal>()?.context?.hasValidToken() == false ->
                UnauthorizedException()
            else -> null
          }
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
    val kommunenavn: String,
    val kontaktpersoner: Kontaktpersoner
)

@Serializable
data class Kontaktpersoner(
    val fagansvarligEpost: List<String>,
    val tekniskAnsvarligEpost: List<String>
)
