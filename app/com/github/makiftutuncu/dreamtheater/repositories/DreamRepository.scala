package com.github.makiftutuncu.dreamtheater.repositories

import java.util.UUID

import anorm._
import com.github.makiftutuncu.dreamtheater.errors.Errors
import com.github.makiftutuncu.dreamtheater.models.Dream
import com.github.makiftutuncu.dreamtheater.models.Dream.dreamRowParser
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import javax.inject.{Inject, Singleton}
import play.api.db.Database

import scala.concurrent.ExecutionContext

@Singleton
class DreamRepository @Inject()(db: Database) extends Repository(db) {
  def getByUserId(userId: UUID, page: Int, pageCount: Int)(implicit ec: ExecutionContext): FM[List[Dream]] =
    withConnection { implicit connection =>
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

      sql.executeQuery().as(dreamRowParser.*)
    } { throwable =>
      Errors.database(s"Cannot get dreams of user '$userId'", throwable)
    }

  def insert(dream: Dream)(implicit ec: ExecutionContext): FM[Dream] =
    withConnection { implicit connection =>
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
    } { throwable =>
      Errors.database(s"Cannot insert dream '${dream.title}' for user '${dream.userId}'", throwable)
    }
}
