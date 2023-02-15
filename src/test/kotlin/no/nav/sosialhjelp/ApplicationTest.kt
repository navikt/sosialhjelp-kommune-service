package no.nav.sosialhjelp

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable
import no.nav.sosialhjelp.fiks.FiksKommuneResponse
import no.nav.sosialhjelp.fiks.Kontaktpersoner
import no.nav.sosialhjelp.utils.MockJwtVerifier
import no.nav.sosialhjelp.utils.withSetup

class ApplicationTest {
  val testKommune =
      FiksKommuneResponse(
          "abc",
          harMidlertidigDeaktivertMottak = false,
          harMidlertidigDeaktivertOppdateringer = false,
          harNksTilgang = true,
          kanMottaSoknader = true,
          kanOppdatereStatus = true,
          kommunenummer = "123",
          kontaktpersoner = Kontaktpersoner(emptyList(), listOf("teknisk@ansvarlig.epost")))
  @Test
  fun `kommuner burde svare med informasjon uten token, saa lenge du ikke spor om kontaktpersoner`() =
      withSetup { client ->
        externalServices {
          hosts("http://localhost:8989") {
            routing {
              this@routing.install(ContentNegotiation) { json() }

              get("/sosialhjelp/mock-alt-api/fiks/digisos/api/v1/nav/kommuner") {
                call.respond(listOf(testKommune))
              }
            }
          }
        }
        client
            .post("/graphql") { setBody("{\"query\": \"{kommuner {kommunenummer}}\"}") }
            .apply {
              assertEquals(HttpStatusCode.OK, status)
              assertEquals(
                  body<GraphQLResponse<Kommuner, Unit>>().data?.kommuner?.get(0)?.kommunenummer,
                  "123")
            }
      }

  @Test
  fun `kommuner burde gi 403 hvis du spor om kontaktpersoner uten token`() = withSetup {
    externalServices {
      hosts("http://localhost:8989") {
        routing {
          this@routing.install(ContentNegotiation) { json() }

          get("/sosialhjelp/mock-alt-api/fiks/digisos/api/v1/nav/kommuner") {
            call.respond(listOf(testKommune))
          }
        }
      }
    }
    client
        .post("/graphql") {
          setBody("{\"query\": \"{ kommuner { kontaktpersoner { tekniskAnsvarligEpost } } }\"}")
        }
        .apply {
          println(this.bodyAsText())
          assertEquals(HttpStatusCode.Unauthorized, status)
          println(bodyAsText())
        }
  }

  @Test
  fun `kommuner burde gi 200 hvis du spor om kontaktpersoner med gyldig token`() =
      withSetup(
          authentication = {
            authentication {
              jwt("azuread") {
                verifier(MockJwtVerifier())
                validate { JWTPrincipal(it.payload) }
              }
            }
          }) { client ->
            externalServices {
              hosts("http://localhost:8989") {
                routing {
                  this@routing.install(ContentNegotiation) { json() }

                  get("/sosialhjelp/mock-alt-api/fiks/digisos/api/v1/nav/kommuner") {
                    call.respond(listOf(testKommune))
                  }
                }
              }
            }
            client
                .post("/graphql") {
                  val token = JWT.create().sign(Algorithm.none())
                  bearerAuth(token)
                  setBody(
                      "{\"query\": \"{ kommuner { kontaktpersoner { tekniskAnsvarligEpost } } }\"}")
                }
                .apply {
                  assertEquals(HttpStatusCode.OK, status)
                  assertEquals(
                      body<GraphQLResponse<Kommuner, Unit>>()
                          .data
                          ?.kommuner
                          ?.firstOrNull()
                          ?.kontaktpersoner
                          ?.tekniskAnsvarligEpost
                          ?.firstOrNull(),
                      "teknisk@ansvarlig.epost")
                }
          }
}

@Serializable private data class GraphQLResponse<T, R>(val data: T? = null, val error: R? = null)

@Serializable private data class Kommuner(val kommuner: List<Kommune>)
