package dev.akif.dreamtheater.session

import anorm._
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.Errors
import dev.akif.dreamtheater.common.base.Repository
import play.api.db.Database

class SessionRepository(db: Database) extends Repository(db) {
  def getByToken(token: String): Z[Option[Session]] =
    withDB { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, user_id, token, created_at, updated_at, deleted_at
            |FROM sessions
            |WHERE token = {token} AND deleted_at IS NULL
          """.stripMargin
        ).on(
          NamedParameter("token", token)
        )

      sql.executeQuery().as(Session.sessionRowParser.singleOpt)
    } {
      case t => Errors.database("Cannot get session by token", t)
    }

  def insert(session: Session): Z[Session] =
    withDB { implicit connection =>
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
    } {
      case t => Errors.database(s"Cannot insert session for user '${session.userId}'", t)
    }
}
