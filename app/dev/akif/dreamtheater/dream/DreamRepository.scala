package dev.akif.dreamtheater.dream

import java.sql.Connection
import java.util.UUID

import anorm._
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.Errors
import dev.akif.dreamtheater.common.base.Repository

import scala.util.control.NonFatal

class DreamRepository extends Repository {
  def getByUserId(userId: UUID, page: Int, pageCount: Int)(implicit connection: Connection): Z[List[Dream]] =
    Z.applyHandlingErrors {
      SQL(
        s"""
           |SELECT id, user_id, title, body, attachment_url, created_at, updated_at, deleted_at
           |FROM dreams
           |WHERE user_id = {userId}::uuid AND deleted_at IS NULL
           |ORDER BY created_at DESC
           |LIMIT $pageCount OFFSET ${page * pageCount}
        """.stripMargin
      )
        .on(NamedParameter("userId", userId))
        .executeQuery()
        .as(Dream.dreamRowParser.*)
    } {
      case NonFatal(t) =>
        Errors.database(s"Cannot get dreams of user").data("userId", userId.toString).cause(t)
    }

  def insert(dream: Dream)(implicit connection: Connection): Z[Dream] =
    Z.applyHandlingErrors {
      SQL(
        """
          |INSERT INTO dreams(id, user_id, title, body, attachment_url, created_at, updated_at, deleted_at)
          |VALUES({id}::uuid, {userId}::uuid, {title}, {body}, {attachmentURL}, {createdAt}, {updatedAt}, {deletedAt})
        """.stripMargin
      )
        .on(
            NamedParameter("id",            dream.id),
            NamedParameter("userId",        dream.userId),
            NamedParameter("title",         dream.title),
            NamedParameter("body",          dream.body),
            NamedParameter("attachmentURL", dream.attachmentURL),
            NamedParameter("createdAt",     dream.createdAt),
            NamedParameter("updatedAt",     dream.updatedAt),
            NamedParameter("deletedAt",     dream.deletedAt),
        )
        .executeUpdate()

      dream
    } {
      case NonFatal(t) =>
        Errors.database(s"Cannot create dream").data("userId", dream.userId.toString).data("title", dream.title).cause(t)
    }
}
