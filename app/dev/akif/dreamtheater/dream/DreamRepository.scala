package dev.akif.dreamtheater.dream

import java.util.UUID

import anorm._
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.Errors
import dev.akif.dreamtheater.common.base.Repository
import play.api.db.Database

class DreamRepository(db: Database) extends Repository(db) {
  def getByUserId(userId: UUID, page: Int, pageCount: Int): Z[List[Dream]] =
    withDB { implicit connection =>
      val sql =
        SQL(
          s"""
            |SELECT id, user_id, title, body, attachment_url, created_at, updated_at, deleted_at
            |FROM dreams
            |WHERE user_id = {userId}::uuid AND deleted_at IS NULL
            |ORDER BY created_at DESC
            |LIMIT $pageCount OFFSET ${page * pageCount}
          """.stripMargin
        ).on(
          NamedParameter("userId", userId)
        )

      sql.executeQuery().as(Dream.dreamRowParser.*)
    } {
      case t => Errors.database(s"Cannot get dreams of user '$userId'", t)
    }

  def insert(dream: Dream): Z[Dream] =
    withDB { implicit connection =>
      val sql =
        SQL(
          """
            |INSERT INTO dreams(id, user_id, title, body, attachment_url, created_at, updated_at, deleted_at)
            |VALUES({id}::uuid, {userId}::uuid, {title}, {body}, {attachmentURL}, {createdAt}, {updatedAt}, {deletedAt})
          """.stripMargin
        ).on(
          NamedParameter("id",            dream.id),
          NamedParameter("userId",        dream.userId),
          NamedParameter("title",         dream.title),
          NamedParameter("body",          dream.body),
          NamedParameter("attachmentURL", dream.attachmentURL),
          NamedParameter("createdAt",     dream.createdAt),
          NamedParameter("updatedAt",     dream.updatedAt),
          NamedParameter("deletedAt",     dream.deletedAt),
      )

      sql.executeUpdate()

      dream
    } {
      case t => Errors.database(s"Cannot insert dream '${dream.title}' for user '${dream.userId}'", t)
    }
}
