package com.github.makiftutuncu.dreamtheater.views

import play.api.libs.json.{JsObject, Json, Reads, Writes}

final case class LoginUserRequest(email: String, password: String) {
  override def toString: String = Json.toJson(this).toString
}

object LoginUserRequest {
  implicit val reads: Reads[LoginUserRequest] = Json.reads[LoginUserRequest]

  implicit val writes: Writes[LoginUserRequest] =
    Json.writes[LoginUserRequest].transform { json: JsObject =>
      json - "password"
    }
}
