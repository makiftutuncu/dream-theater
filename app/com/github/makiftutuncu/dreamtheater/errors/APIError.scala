package com.github.makiftutuncu.dreamtheater.errors

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsNull, JsValue, Json, Writes}

final case class APIError(status: Int,
                          message: String,
                          data: JsValue,
                          maybeCause: Option[Throwable]) extends Exception(message, maybeCause.orNull, false, maybeCause.isDefined) {
  lazy val asJson: JsValue = Json.obj("error" -> Json.toJson(this))
}

object APIError {
  implicit val apiErrorWrites: Writes[APIError] =
    Writes { apiError =>
      Json.obj(
        "message" -> apiError.message,
        "data"    -> apiError.data
      )
    }

  def from(t: Throwable): APIError = APIError(INTERNAL_SERVER_ERROR, t.getMessage, JsNull, Some(t))
}
