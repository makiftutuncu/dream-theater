package dev.akif.dreamtheater.session

import java.util.UUID

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.ZDT
import dev.akif.dreamtheater.common.base.Service
import play.api.Logging

class SessionService(sessionRepository: SessionRepository) extends Service with Logging {
  def getByToken(token: String): Z[Option[Session]] = sessionRepository.getByToken(token)

  def create(userId: UUID): Z[Session] =
    sessionRepository.insert(
      Session(
        id        = UUID.randomUUID,
        userId    = userId,
        token     = (UUID.randomUUID.toString + UUID.randomUUID.toString).replaceAll("-", ""),
        createdAt = ZDT.now,
        updatedAt = ZDT.now,
        deletedAt = None
      )
    )
}
