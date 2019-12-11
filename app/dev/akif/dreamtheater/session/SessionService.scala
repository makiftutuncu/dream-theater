package dev.akif.dreamtheater.session

import java.util.UUID

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.ZDT
import dev.akif.dreamtheater.common.base.Service
import play.api.db.Database

class SessionService(sessionRepository: SessionRepository, db: Database) extends Service(db) {
  def getByToken(token: String): Z[Option[Session]] =
    withDB { implicit connection =>
      sessionRepository.getByToken(token)
    }

  def create(userId: UUID): Z[Session] =
    withDB { implicit connection =>
      sessionRepository.insert(
        buildSession(userId)
      )
    }

  def buildSession(userId: UUID): Session =
    Session(
      id        = UUID.randomUUID,
      userId    = userId,
      token     = (UUID.randomUUID.toString + UUID.randomUUID.toString).replaceAll("-", ""),
      createdAt = ZDT.now,
      updatedAt = ZDT.now,
      deletedAt = None
    )
}
