package no.nav.sosialhjelp.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import no.nav.sosialhjelp.utils.Config

fun Application.configureSecurity() {
  val azureADConfig = Config.AzureADConfig
  authentication {
    jwt("azuread") {
      verifier(jwkProvider = azureADConfig.jwkProvider, issuer = azureADConfig.issuer) {
        this@configureSecurity.log.info("Verifying azuread jwt with ${azureADConfig.issuer}")
        withAudience(azureADConfig.clientId)
      }
      validate {
        this@configureSecurity.log.info("Validating jwt")
        JWTPrincipal(it.payload)
      }
    }
  }
}
