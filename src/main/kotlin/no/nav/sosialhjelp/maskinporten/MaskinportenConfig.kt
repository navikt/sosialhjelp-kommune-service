package no.nav.sosialhjelp.maskinporten

import com.nimbusds.jose.jwk.RSAKey
import no.nav.sosialhjelp.utils.getEnvVar

data class MaskinportenConfig(
    val tokenEndpointUrl: String = getEnvVar(),
    val clientId: String,
    val privateKey: RSAKey,
    val scope: String,
    val resource: String,
    val issuer: String,
    val expireAfterSec: Long = 120
) {
  init {
    require(expireAfterSec <= 120) { "Maskinporten allows a maximum of 120 seconds expiry" }
  }
}
