package dev.akif.dreamtheater.dream

import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, SqlMappingError}
import dev.akif.dreamtheater.common.base.Model
import play.api.libs.json.{JsObject, Json, Writes}

import scala.util.Try

final case class Dream(override val id: UUID,
                       userId: UUID,
                       title: String,
                       body: String,
                       attachmentURL: Option[String],
                       override val createdAt: ZonedDateTime,
                       override val updatedAt: ZonedDateTime,
                       override val deletedAt: Option[ZonedDateTime]) extends Model

object Dream {
  implicit val dreamWrites: Writes[Dream] =
    Json.writes[Dream].transform { json: JsObject =>
      json - "deletedAt"
    }

  implicit val dreamRowParser: RowParser[Dream] =
    RowParser[Dream] { row =>
      Try {
        Dream(
          row[UUID]("id"),
          row[UUID]("user_id"),
          row[String]("title"),
          row[String]("body"),
          row[Option[String]]("attachment_url"),
          row[ZonedDateTime]("created_at"),
          row[ZonedDateTime]("updated_at"),
          row[Option[ZonedDateTime]]("deleted_at")
        )
      }.fold(
        t => anorm.Error(SqlMappingError(t.getMessage)),
        u => anorm.Success(u)
      )
    }
}
