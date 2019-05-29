package com.github.makiftutuncu.dreamtheater.views

import java.time.LocalDate

import com.github.makiftutuncu.dreamtheater.models.Gender
import play.api.libs.json.{JsObject, Json, Reads, Writes}

final case class RegisterUserRequest(email: String,
                                     password: String,
                                     firstName: Option[String],
                                     lastName: Option[String],
                                     gender: Option[Gender],
                                     birthDate: Option[LocalDate]) {
  override def toString: String = Json.toJson(this).toString
}

object RegisterUserRequest {
  implicit val registerUserRequestReads: Reads[RegisterUserRequest] = Json.reads[RegisterUserRequest]

  implicit val registerUserRequestWrites: Writes[RegisterUserRequest] =
    Json.writes[RegisterUserRequest].transform { json: JsObject =>
      json - "password"
    }
}
