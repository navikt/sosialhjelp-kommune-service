package no.nav.sosialhjelp.maskinporten

import com.nimbusds.jwt.SignedJWT
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.http.ContentType.Application.FormUrlEncoded
import io.ktor.serialization.kotlinx.json.json
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
const val LEEWAY_SECONDS: Long = 20

interface Oauth2JwtProvider {
  suspend fun getToken(): String
}

class HttpClientMaskinportenTokenProvider(
    private val config: MaskinportenConfig,
) : Oauth2JwtProvider {
  private val log = LoggerFactory.getLogger(this::class.java)
  private val grants: JwtGrantFactory = JwtGrantFactory(config)
  private val cache = mutableMapOf<String, Token>()
  private val client = HttpClient {
    install(ContentNegotiation) {
      json(
          Json {
            ignoreUnknownKeys = true
            isLenient = true
          })
    }
  }

  override suspend fun getToken(): String {
    val token = cache[config.scope]?.takeUnless(Token::hasExpired) ?: fetchToken()

    return token.access_token.let(SignedJWT::parse).parsedString
  }

  private suspend fun fetchToken(): Token {
    log.info("Henter token fra '${config.tokenEndpointUrl}'")
    return runCatching {
          val response =
              client.post(config.tokenEndpointUrl) {
                expectSuccess = true
                contentType(FormUrlEncoded)
                setBody("grant_type=$GRANT_TYPE&assertion=${grants.jwt}")
              }
          when (response.status.value) {
            in 200..299 -> {
              log.info("Fikk token fra maskinporten.")
              response.body<Token>()
            }
            in 400..499 -> {
              log.error("Fikk client error fra maskinporten")
              throw ClientRequestException(response, response.bodyAsText())
            }
            in 500..599 -> {
              log.error("Fikk server error fra maskinporten")
              throw ServerResponseException(response, response.bodyAsText())
            }
            else -> throw IllegalStateException("weeeeh")
          }
        }
        .onFailure { log.error("Feil ved henting av token: ", it) }
        .onSuccess { token ->
          log.info("Cacher token")
          cache[config.scope] = token
        }
        .getOrThrow()
  }
}

@Serializable
private data class Token(val access_token: String) {
  private val signedJwt
    get() = SignedJWT.parse(access_token)

  private val expiry
    get() = signedJwt.jwtClaimsSet.expirationTime.toInstant().minusSeconds(LEEWAY_SECONDS)

  fun hasExpired() = expiry < Instant.now()
}
