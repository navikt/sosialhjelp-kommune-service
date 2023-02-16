package no.nav.sosialhjelp.external

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import org.slf4j.LoggerFactory

class Response<T>(private val response: HttpResponse, block: Response<T>.() -> Response<T>) {

  val logger = LoggerFactory.getLogger(this::class.java)
  init {
    block()
  }

  private lateinit var on2xx: suspend (response: HttpResponse) -> T

  private var on4xx: (response: HttpResponse) -> T = {
    logger.error("Fikk 4xx fra ${response.request.url}")
    throw ClientRequestException(response, response.status.description)
  }

  private var on5xx: (response: HttpResponse) -> Nothing = {
    logger.error("Fikk 5xx fra ${response.request.url}")
    throw ServerResponseException(response, response.status.description)
  }

  fun on2xx(block: suspend (response: HttpResponse) -> T): Response<T> {
    on2xx = block
    return this
  }

  fun on4xx(block: (response: HttpResponse) -> T): Response<T> {
    on4xx = block
    return this
  }

  fun on5xx(block: (response: HttpResponse) -> Nothing): Response<T> {
    on5xx = block
    return this
  }

  suspend fun respond(): T =
      when (response.status.value) {
        in 200..299 -> on2xx(response)
        in 400..499 -> on4xx(response)
        in 500..599 -> on5xx(response)
        else -> error("Uhåndtert status i respons: ${response.status}")
      }

  suspend fun respondOrNull(): T? = if (response.status.value in 200..299) on2xx(response) else null
}
