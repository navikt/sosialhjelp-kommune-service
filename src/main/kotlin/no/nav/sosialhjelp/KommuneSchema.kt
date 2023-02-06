package no.nav.sosialhjelp

import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import no.nav.sosialhjelp.fiks.getAllFiksKommuner
import no.nav.sosialhjelp.fiks.getAllGeodataKommuner
import no.nav.sosialhjelp.plugins.TokenValidationContextPrincipal

suspend fun SchemaBuilder.kommuneSchema() =
    withContext(Dispatchers.IO) {
      val httpClient = HttpClient()
      query("kommuner") {
        description = "Alle kommuner"
        resolver { ->
          val fiksKommunerDeferred = async { getAllFiksKommuner(httpClient) }
          val geodataKommunerDeferred = async { getAllGeodataKommuner(httpClient) }
          val fiksKommuner = fiksKommunerDeferred.await()
          val geodataKommune = geodataKommunerDeferred.await()
            return@resolver ""
        }
      }

      type<Kommune> {
        property<Kontaktpersoner>("kontaktpersoner") {
          description = "Informasjon om kontaktpersoner i kommunen"
          accessRule { _: Kommune, context: Context ->
            if (context.get<TokenValidationContextPrincipal>() == null)
                IllegalArgumentException("fekk")
            else null
          }
        }
      }
      type<Kontaktpersoner>()
    }

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
