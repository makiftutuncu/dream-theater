package dev.akif.dreamtheater.common

import dev.akif.e.E
import play.api.Logging
import play.api.http.ContentTypes
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Results.Status
import play.api.mvc.{Result, Results}

trait ActionUtils extends Logging {
  def fail(e: E): Result = Status(e.code)(e.toString).as(ContentTypes.JSON)

  def succeed[A: Writes](value: A, status: Status = Results.Ok): Result = status(Json.toJson(value))
}
