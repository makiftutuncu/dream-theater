package com.github.makiftutuncu.dreamtheater.services

import java.util.UUID

import com.github.makiftutuncu.dreamtheater.models.Dream
import com.github.makiftutuncu.dreamtheater.repositories.DreamRepository
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import com.github.makiftutuncu.dreamtheater.utilities.ZDT
import com.github.makiftutuncu.dreamtheater.views.CreateDreamRequest
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.ExecutionContext

@Singleton
class DreamService @Inject()(dreamRepository: DreamRepository) extends Service with Logging {
  def getDreams(userId: UUID, page: Int, pageCount: Int)(implicit ec: ExecutionContext): FM[List[Dream]] = dreamRepository.getByUserId(userId, page, pageCount)

  def create(userId: UUID, request: CreateDreamRequest)(implicit ec: ExecutionContext): FM[Dream] = {
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
