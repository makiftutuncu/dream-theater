package dev.akif.dreamtheater.session

import java.sql.Connection

import anorm._
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.base.Repository

class SessionRepository extends Repository {
  def getByToken(token: String)(implicit connection: Connection): Z[Option[Session]] =
    Z {
      SQL(
        """
          |SELECT id, user_id, token, created_at, updated_at, deleted_at
          |FROM sessions
          |WHERE token = {token} AND deleted_at IS NULL
        """.stripMargin
      )
        .on(NamedParameter("token", token))
        .executeQuery()
        .as(Session.sessionRowParser.singleOpt)
    }

  def insert(session: Session)(implicit connection: Connection): Z[Session] =
    Z {
      SQL(
        """
          |INSERT INTO sessions(id, user_id, token, created_at, updated_at, deleted_at)
          |VALUES({id}::uuid, {userId}::uuid, {token}, {createdAt}, {updatedAt}, {deletedAt})
        """.stripMargin
      )
        .on(
          NamedParameter("id",        session.id),
          NamedParameter("userId",    session.userId),
          NamedParameter("token",     session.token),
          NamedParameter("createdAt", session.createdAt),
          NamedParameter("updatedAt", session.updatedAt),
          NamedParameter("deletedAt", session.deletedAt),
        )
        .executeUpdate()

      session
    }
}
