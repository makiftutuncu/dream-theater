package com.github.makiftutuncu.dreamtheater.errors

import play.api.http.Status
import play.api.libs.json.JsNull

object Errors {
  val invalidLogin: APIError = APIError(Status.UNAUTHORIZED, s"Invalid login credentials!", JsNull, None)

  def database(details: String): APIError               = APIError(Status.INTERNAL_SERVER_ERROR, details, JsNull, None)
  def database(details: String, t: Throwable): APIError = APIError(Status.INTERNAL_SERVER_ERROR, s"$details: ${t.getMessage}", JsNull, Some(t))
}
