package com.github.makiftutuncu.dreamtheater.repositories

import anorm._
import com.github.makiftutuncu.dreamtheater.errors.Errors
import com.github.makiftutuncu.dreamtheater.models.Session
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import javax.inject.{Inject, Singleton}
import play.api.db.Database

import scala.concurrent.ExecutionContext

@Singleton
class SessionRepository @Inject()(db: Database) extends Repository(db) {
  def getByToken(token: String)(implicit ec: ExecutionContext): FM[Option[Session]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT *
            |FROM sessions
            |WHERE token = {token} AND deleted_at IS NULL
          """.stripMargin
        ).on(
          NamedParameter("token", token)
        )

      sql.executeQuery().as(Session.rowParser.singleOpt)
    } { throwable =>
      Errors.database("Cannot get session by token", throwable)
    }

  def insert(session: Session)(implicit ec: ExecutionContext): FM[Session] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |INSERT INTO sessions(id, user_id, token, created_at, updated_at, deleted_at)
            |VALUES({id}::uuid, {userId}::uuid, {token}, {createdAt}, {updatedAt}, {deletedAt})
          """.stripMargin
        ).on(
          NamedParameter("id",        session.id),
          NamedParameter("userId",    session.userId),
          NamedParameter("token",     session.token),
          NamedParameter("createdAt", session.createdAt),
          NamedParameter("updatedAt", session.updatedAt),
          NamedParameter("deletedAt", session.deletedAt),
      )

      sql.executeUpdate()

      session
    } { throwable =>
      Errors.database(s"Cannot insert session for user '${session.userId}'", throwable)
    }
}
