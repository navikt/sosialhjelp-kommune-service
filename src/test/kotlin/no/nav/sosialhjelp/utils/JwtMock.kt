package no.nav.sosialhjelp.utils

import com.auth0.jwt.impl.NullClaim
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import java.time.Instant
import java.util.Date

class MockDecodedJWT : DecodedJWT {
  override fun getIssuer(): String = "issuer"
  override fun getSubject(): String = "subject"
  override fun getAudience(): MutableList<String> = mutableListOf("aud")
  override fun getExpiresAt(): Date = Date.from(Instant.now().plusSeconds(100))
  override fun getNotBefore(): Date = Date.from(Instant.now())
  override fun getIssuedAt(): Date = Date.from(Instant.now())
  override fun getId(): String = "id"
  override fun getClaim(name: String?): Claim = NullClaim()
  override fun getClaims(): MutableMap<String, Claim> = mutableMapOf()
  override fun getAlgorithm(): String = "alg"
  override fun getType(): String = "type"
  override fun getContentType(): String = "application/json"
  override fun getKeyId(): String = "key_id"
  override fun getHeaderClaim(name: String?): Claim = NullClaim()
  override fun getToken(): String = "token"
  override fun getHeader(): String = "header"
  override fun getPayload(): String = "eyJwYXlsb2FkIjogImZla2sifQ=="
  override fun getSignature(): String = "sig"
}

class MockJwtVerifier : JWTVerifier {
  override fun verify(token: String?): DecodedJWT {
    return MockDecodedJWT()
  }
  override fun verify(jwt: DecodedJWT?): DecodedJWT {
    return MockDecodedJWT()
  }
}
