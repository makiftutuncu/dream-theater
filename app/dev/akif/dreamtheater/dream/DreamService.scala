package dev.akif.dreamtheater.dream

import java.util.UUID

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.ZDT
import dev.akif.dreamtheater.common.base.Service
import play.api.Logging

class DreamService(dreamRepository: DreamRepository) extends Service with Logging {
  def getDreams(userId: UUID, page: Int, pageCount: Int): Z[List[Dream]] = dreamRepository.getByUserId(userId, page, pageCount)

  def create(userId: UUID, request: CreateDreamRequest): Z[Dream] = {
    val newDream = Dream(
      id            = UUID.randomUUID,
      userId        = userId,
      title         = request.title,
      body          = request.body,
      attachmentURL = request.attachmentURL,
      createdAt     = ZDT.now,
      updatedAt     = ZDT.now,
      deletedAt     = None
    )

    dreamRepository.insert(newDream)
  }
}
