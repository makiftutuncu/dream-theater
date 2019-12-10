package dev.akif.dreamtheater.common

import dev.akif.e.E
import play.api.http.Status

object Errors {
  val invalidBody: E = E.of(Status.BAD_REQUEST, "invalid-body")

  def unauthorized(message: String): E = E.of(Status.UNAUTHORIZED, "unauthorized", message)
  val invalidLogin: E                  = E.of(Status.UNAUTHORIZED, "unauthorized", "Invalid login credentials")

  def database(message: String): E                   = E.of(Status.INTERNAL_SERVER_ERROR, "database", message)
  def database(message: String, cause: Throwable): E = E.of(Status.INTERNAL_SERVER_ERROR, "database", message, cause)
}
