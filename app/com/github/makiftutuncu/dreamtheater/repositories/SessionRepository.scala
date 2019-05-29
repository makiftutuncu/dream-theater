package com.github.makiftutuncu.dreamtheater.repositories

import anorm._
import com.github.makiftutuncu.dreamtheater.errors.{APIError, Errors}
import com.github.makiftutuncu.dreamtheater.models.Session
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class SessionRepository @Inject()(db: Database) extends Repository(db) with Logging {
  def getByToken(token: String)(implicit ec: ExecutionContext): Future[Either[APIError, Option[Session]]] =
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

      Right(sql.executeQuery().as(Session.sessionRowParser.singleOpt))
    }.recover {
      case NonFatal(t) =>
        val error = Errors.database("Cannot get session by token", t)
        logger.error(error.message, error)
        Left(error)
    }

  def insert(session: Session)(implicit ec: ExecutionContext): Future[Either[APIError, Session]] =
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

      Right(session)
    }.recover {
      case NonFatal(t) =>
        val error = Errors.database(s"Cannot insert session for user '${session.userId}'", t)
        logger.error(error.message, error)
        Left(error)
    }
}
