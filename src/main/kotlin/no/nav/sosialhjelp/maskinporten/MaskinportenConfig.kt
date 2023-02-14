package no.nav.sosialhjelp.maskinporten

import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import java.util.UUID
import no.nav.sosialhjelp.utils.Config
import no.nav.sosialhjelp.utils.Env

data class MaskinportenConfig(
    val tokenEndpointUrl: String,
    val issuer: String,
    val clientId: String = Config.Maskinporten.clientId,
    val privateKey: RSAKey =
        if (Config.Maskinporten.clientJwk == "generateRSA") {
          if (Config.env == Env.PROD) error("Generation of RSA keys is not allowed in prod")
          RSAKeyGenerator(2048)
              .keyUse(KeyUse.SIGNATURE)
              .keyID(UUID.randomUUID().toString())
              .generate()
        } else {
          RSAKey.parse(Config.Maskinporten.clientJwk)
        },
    val scope: String = Config.Maskinporten.scopes,
    val resource: String = "??",
    val expireAfterSec: Long = 120
) {
  init {
    require(expireAfterSec <= 120) { "Maskinporten allows a maximum of 120 seconds expiry" }
  }
}
