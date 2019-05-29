package com.github.makiftutuncu.dreamtheater.views

import java.time.LocalDate

import com.github.makiftutuncu.dreamtheater.models.Gender
import play.api.libs.json.{Json, Reads}

final case class RegisterUserRequest(email: String,
                                     password: String,
                                     firstName: Option[String],
                                     lastName: Option[String],
                                     gender: Option[Gender],
                                     birthDate: Option[LocalDate])

object RegisterUserRequest {
  implicit val registerUserRequestReads: Reads[RegisterUserRequest] = Json.reads[RegisterUserRequest]
}
