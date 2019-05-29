package com.github.makiftutuncu.dreamtheater.services

import java.util.UUID

import com.github.makiftutuncu.dreamtheater.models.Session
import com.github.makiftutuncu.dreamtheater.repositories.SessionRepository
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import com.github.makiftutuncu.dreamtheater.utilities.ZDT
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.ExecutionContext

@Singleton
class SessionService @Inject()(sessionRepository: SessionRepository) extends Service with Logging {
  def getByToken(token: String)(implicit ec: ExecutionContext): FM[Option[Session]] = sessionRepository.getByToken(token)

  def create(userId: UUID)(implicit ec: ExecutionContext): FM[Session] =
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
