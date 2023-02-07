package no.nav.sosialhjelp

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.apurebase.kgraphql.schema.execution.Executor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nidomiro.kdataloader.ExecutionResult
import no.nav.sosialhjelp.fiks.getAllFiksKommuner
import no.nav.sosialhjelp.fiks.getAllGeodataKommuner
import no.nav.sosialhjelp.plugins.TokenValidationContextPrincipal
import no.nav.sosialhjelp.utils.Env
import no.nav.sosialhjelp.utils.Environment

val httpClient = HttpClient {
  install(Logging) {
    logger = Logger.DEFAULT
    level = LogLevel.HEADERS
  }
  install(ContentNegotiation) {
    json(
        Json {
          prettyPrint = true
          isLenient = true
        })
  }
}

fun SchemaBuilder.kommuneSchema() {
  configure { executor = Executor.DataLoaderPrepared }

  query("kommuner") {
    description = "Alle kommuner"
    resolver { ->
      withContext(Dispatchers.IO) {
        val fiksKommunerDeferred = async { getAllFiksKommuner(httpClient) }
        val fiksKommuner = fiksKommunerDeferred.await()
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
          )
        }
      }
    }

    type<Kommune> {
      dataProperty("kommunenavn") {
        prepare { kommune -> kommune.kommunenummer }
        loader { ids ->
          val kommuner =
              getAllGeodataKommuner(httpClient).associate {
                it.kommunenummer to it.kommunenavnNorsk
              }
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
