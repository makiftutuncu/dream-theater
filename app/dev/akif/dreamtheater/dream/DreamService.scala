package dev.akif.dreamtheater.dream

import java.util.UUID

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.ZDT
import dev.akif.dreamtheater.common.base.Service
import play.api.db.Database

class DreamService(dreamRepository: DreamRepository, db: Database) extends Service(db) {
  def getDreams(userId: UUID, page: Int, pageCount: Int): Z[List[Dream]] =
    withDB { implicit connection =>
      dreamRepository.getByUserId(userId, page, pageCount)
    }

  def create(userId: UUID, request: CreateDreamRequest): Z[Dream] =
    withDB { implicit connection =>
      dreamRepository.insert(buildDream(userId, request))
    }

  def buildDream(userId: UUID, request: CreateDreamRequest): Dream =
    Dream(
      id            = UUID.randomUUID,
      userId        = userId,
      title         = request.title,
      body          = request.body,
      attachmentURL = request.attachmentURL,
      createdAt     = ZDT.now,
      updatedAt     = ZDT.now,
      deletedAt     = None
    )
}
