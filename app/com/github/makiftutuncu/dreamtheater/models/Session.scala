package com.github.makiftutuncu.dreamtheater.models

import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, SqlMappingError, Success}
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
  implicit val sessionWrites: Writes[Session] =
    Json.writes[Session].transform { json: JsObject =>
      json - "token" - "deletedAt"
    }

  implicit val sessionRowParser: RowParser[Session] =
    RowParser[Session] { row =>
      Try {
        val user = Session(
          row[UUID]("id"),
          row[UUID]("user_id"),
          row[String]("token"),
          row[ZonedDateTime]("created_at"),
          row[ZonedDateTime]("updated_at"),
          row[Option[ZonedDateTime]]("deleted_at")
        )

        Success(user)
      }.fold(
        t => anorm.Error(SqlMappingError(t.getMessage)),
        identity
      )
    }
}
