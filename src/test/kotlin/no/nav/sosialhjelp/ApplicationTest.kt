package no.nav.sosialhjelp

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import no.nav.sosialhjelp.plugins.configureRouting

class ApplicationTest {
  @Test
  fun testRoot() = testApplication {
    application { configureRouting { "" } }
    client.get("/internal/is_ready").apply { assertEquals(HttpStatusCode.OK, status) }
  }
}
