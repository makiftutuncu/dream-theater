package com.github.makiftutuncu.dreamtheater.views

import play.api.libs.json.{Json, Reads}

final case class LoginUserRequest(email: String, password: String)

object LoginUserRequest {
  implicit val loginUserRequestReads: Reads[LoginUserRequest] = Json.reads[LoginUserRequest]
}
