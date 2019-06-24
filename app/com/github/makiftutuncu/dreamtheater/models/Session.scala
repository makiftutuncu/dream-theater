package com.github.makiftutuncu.dreamtheater.models

import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, SqlMappingError}
import play.api.libs.json.{JsObject, Json, Writes}

import scala.util.Try

final case class Session(override val id: UUID,
                         userId: UUID,
                         token: String,
                         override val createdAt: ZonedDateTime,
                         override val updatedAt: ZonedDateTime,
                         override val deletedAt: Option[ZonedDateTime]) extends Model {
  override def toString: String = Json.toJson(this).toString
}

object Session {
  val AUTH_TOKEN_HEADER_NAME = "X-Auth-Token"

  implicit val sessionWrites: Writes[Session] =
    Json.writes[Session].transform { json: JsObject =>
      json - "token" - "deletedAt"
    }

  implicit val sessionRowParser: RowParser[Session] =
    RowParser[Session] { row =>
      Try {
        Session(
          row[UUID]("id"),
          row[UUID]("user_id"),
          row[String]("token"),
          row[ZonedDateTime]("created_at"),
          row[ZonedDateTime]("updated_at"),
          row[Option[ZonedDateTime]]("deleted_at")
        )
      }.fold(
        t => anorm.Error(SqlMappingError(t.getMessage)),
        s => anorm.Success(s)
      )
    }
}
