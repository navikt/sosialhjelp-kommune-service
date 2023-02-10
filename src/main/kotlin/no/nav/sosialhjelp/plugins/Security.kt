package no.nav.sosialhjelp.plugins

import io.ktor.http.CookieEncoding
import io.ktor.http.Headers
import io.ktor.http.decodeCookieValue
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.request.RequestCookies
import io.ktor.server.response.header
import io.ktor.server.response.respond
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import no.nav.security.token.support.core.JwtTokenConstants
import no.nav.security.token.support.core.configuration.IssuerProperties
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.exceptions.JwtTokenInvalidClaimException
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.core.http.HttpRequest
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.security.token.support.core.utils.JwtTokenUtil.getJwtToken
import no.nav.security.token.support.core.validation.JwtTokenAnnotationHandler
import no.nav.security.token.support.core.validation.JwtTokenValidationHandler
import org.slf4j.LoggerFactory

fun Application.configureSecurity() {
//  authentication { tokenValidationSupport("tokenx", TokenSupportConfig(), null) }
}

class JwtTokenExpiryThresholdHandler(private val expiryThreshold: Int) {

  private val log = LoggerFactory.getLogger(JwtTokenExpiryThresholdHandler::class.java.name)

  fun addHeaderOnTokenExpiryThreshold(
      call: ApplicationCall,
      tokenValidationContext: TokenValidationContext
  ) {
    if (expiryThreshold > 0) {
      tokenValidationContext.issuers.forEach { issuer ->
        val jwtTokenClaims = tokenValidationContext.getClaims(issuer)
        if (tokenExpiresBeforeThreshold(jwtTokenClaims)) {
          call.response.header(JwtTokenConstants.TOKEN_EXPIRES_SOON_HEADER, "true")
        } else {
          log.debug("Token is still within expiry threshold.")
        }
      }
    } else {
      log.debug("Expiry threshold is not set")
    }
  }

  private fun tokenExpiresBeforeThreshold(jwtTokenClaims: JwtTokenClaims): Boolean {
    val expiryDate = jwtTokenClaims["exp"] as Date
    val expiry = LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault())
    val minutesUntilExpiry = LocalDateTime.now().until(expiry, ChronoUnit.MINUTES)
    log.debug(
        "Checking token at time {} with expirationTime {} for how many minutes until expiry: {}",
        LocalDateTime.now(),
        expiry,
        minutesUntilExpiry)
    if (minutesUntilExpiry <= expiryThreshold) {
      log.debug(
          "There are {} minutes until expiry which is equal to or less than the configured threshold {}",
          minutesUntilExpiry,
          expiryThreshold)
      return true
    }
    return false
  }
}

data class TokenValidationContextPrincipal(val context: TokenValidationContext) : Principal

private val log = LoggerFactory.getLogger(TokenSupportAuthenticationProvider::class.java.name)

class TokenSupportAuthenticationProvider(
    providerConfig: ProviderConfiguration,
    applicationConfig: ApplicationConfig,
    private val requiredClaims: RequiredClaims? = null,
    private val additionalValidation: ((TokenValidationContext) -> Boolean)? = null,
    resourceRetriever: ProxyAwareResourceRetriever
) : AuthenticationProvider(providerConfig) {

  private val jwtTokenValidationHandler: JwtTokenValidationHandler
  private val jwtTokenExpiryThresholdHandler: JwtTokenExpiryThresholdHandler

  init {
    val issuerPropertiesMap: Map<String, IssuerProperties> = applicationConfig.asIssuerProps()
    jwtTokenValidationHandler =
        JwtTokenValidationHandler(MultiIssuerConfiguration(issuerPropertiesMap, resourceRetriever))

    val expiryThreshold: Int =
        applicationConfig
            .propertyOrNull("no.nav.security.jwt.expirythreshold")
            ?.getString()
            ?.toInt()
            ?: -1
    jwtTokenExpiryThresholdHandler = JwtTokenExpiryThresholdHandler(expiryThreshold)
  }

  class ProviderConfiguration internal constructor(name: String?) : Config(name)

  override suspend fun onAuthenticate(context: AuthenticationContext) {
    val applicationCall = context.call
    val tokenValidationContext =
        jwtTokenValidationHandler.getValidatedTokens(
            JwtTokenHttpRequest(applicationCall.request.cookies, applicationCall.request.headers))
    try {
      if (tokenValidationContext.hasValidToken()) {
        if (requiredClaims != null) {
          RequiredClaimsHandler(InternalTokenValidationContextHolder(tokenValidationContext))
              .handleRequiredClaims(requiredClaims)
        }
        if (additionalValidation != null) {
          if (!additionalValidation.invoke(tokenValidationContext)) {
            throw AdditionalValidationReturnedFalse()
          }
        }
        jwtTokenExpiryThresholdHandler.addHeaderOnTokenExpiryThreshold(
            applicationCall, tokenValidationContext)
        context.principal(TokenValidationContextPrincipal(tokenValidationContext))
      }
    } catch (e: Throwable) {
      val message = e.message ?: e.javaClass.simpleName
      log.trace("Token verification failed: {}", message)
    }
      // TODO: Er det sikkert nok uten denne?
//    context.challenge(key = "JWTAuthKey", cause = AuthenticationFailedCause.InvalidCredentials) {
//        authenticationProcedureChallenge,
//        call ->
//      call.respond(UnauthorizedResponse())
//      authenticationProcedureChallenge.complete()
//    }
  }
}

fun AuthenticationConfig.tokenValidationSupport(
    name: String? = null,
    config: ApplicationConfig,
    requiredClaims: RequiredClaims? = null,
    additionalValidation: ((TokenValidationContext) -> Boolean)? = null,
    resourceRetriever: ProxyAwareResourceRetriever =
        ProxyAwareResourceRetriever(System.getenv("HTTP_PROXY")?.let { URL(it) })
) {
  val provider =
      TokenSupportAuthenticationProvider(
          providerConfig = TokenSupportAuthenticationProvider.ProviderConfiguration(name),
          applicationConfig = config,
          requiredClaims = requiredClaims,
          additionalValidation = additionalValidation,
          resourceRetriever = resourceRetriever)

  register(provider)
}

data class RequiredClaims(
    val issuer: String,
    val claimMap: List<String>,
    val combineWithOr: Boolean = false
)

data class IssuerConfig(
    val name: String,
    val discoveryUrl: String,
    val acceptedAudience: List<String>,
    val cookieName: String? = null
)

class TokenSupportConfig(vararg issuers: IssuerConfig) :
    MapApplicationConfig(
        issuers
            .mapIndexed { index, issuerConfig ->
              listOf(
                      "no.nav.security.jwt.issuers.$index.issuer_name" to issuerConfig.name,
                      "no.nav.security.jwt.issuers.$index.discoveryurl" to
                          issuerConfig.discoveryUrl,
                      "no.nav.security.jwt.issuers.$index.accepted_audience" to
                          issuerConfig.acceptedAudience.joinToString(",") // ,
                      )
                  .let {
                    if (issuerConfig.cookieName != null) {
                      it.plus(
                          "no.nav.security.jwt.issuers.$index.cookie_name" to
                              issuerConfig.cookieName)
                    } else {
                      it
                    }
                  }
            }
            .flatten()
            .plus("no.nav.security.jwt.issuers.size" to issuers.size.toString()))

private class InternalTokenValidationContextHolder(
    private var tokenValidationContext: TokenValidationContext
) : TokenValidationContextHolder {
  override fun getTokenValidationContext() = tokenValidationContext
  override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
    this.tokenValidationContext = tokenValidationContext!!
  }
}

internal class AdditionalValidationReturnedFalse : RuntimeException()

internal class RequiredClaimsException(message: String, cause: Exception) :
    RuntimeException(message, cause)

internal class RequiredClaimsHandler(
    private val tokenValidationContextHolder: TokenValidationContextHolder
) : JwtTokenAnnotationHandler(tokenValidationContextHolder) {
  internal fun handleRequiredClaims(requiredClaims: RequiredClaims) {
    try {
      val jwtToken = getJwtToken(requiredClaims.issuer, tokenValidationContextHolder)
      if (jwtToken.isEmpty) {
        throw JwtTokenMissingException("no valid token found in validation context")
      }
      if (!handleProtectedWithClaims(
          requiredClaims.issuer,
          requiredClaims.claimMap.toTypedArray(),
          requiredClaims.combineWithOr,
          jwtToken.get()))
          throw JwtTokenInvalidClaimException(
              "required claims not present in token." + requiredClaims.claimMap)
    } catch (e: RuntimeException) {
      throw RequiredClaimsException(e.message ?: "", e)
    }
  }
}

internal data class NameValueCookie(@JvmField val name: String, @JvmField val value: String) :
    HttpRequest.NameValue {
  override fun getName(): String = name
  override fun getValue(): String = value
}

internal data class JwtTokenHttpRequest(
    private val cookies: RequestCookies,
    private val headers: Headers
) : HttpRequest {
  override fun getCookies() =
      cookies.rawCookies
          .map { NameValueCookie(it.key, decodeCookieValue(it.value, CookieEncoding.URI_ENCODING)) }
          .toTypedArray()

  override fun getHeader(name: String) = headers[name]
}

fun ApplicationConfig.asIssuerProps(): Map<String, IssuerProperties> =
    this.configList("no.nav.security.jwt.issuers").associate { issuerConfig ->
      issuerConfig.property("issuer_name").getString() to
          IssuerProperties(
              URL(issuerConfig.property("discoveryurl").getString()),
              issuerConfig.property("accepted_audience").getString().split(","),
              issuerConfig.propertyOrNull("cookie_name")?.getString(),
              issuerConfig.propertyOrNull("header_name")?.getString(),
              IssuerProperties.Validation(
                  issuerConfig.propertyOrNull("validation.optional_claims")?.getString()?.split(",")
                      ?: emptyList()),
              IssuerProperties.JwksCache(
                  issuerConfig.propertyOrNull("jwks_cache.lifespan")?.getString()?.toLong(),
                  issuerConfig.propertyOrNull("jwks_cache.refreshtime")?.getString()?.toLong()))
    }
