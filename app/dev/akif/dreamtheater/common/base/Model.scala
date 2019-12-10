package dev.akif.dreamtheater.common.base

import java.time.ZonedDateTime
import java.util.UUID

trait Model {
  val id: UUID
  val createdAt: ZonedDateTime
  val updatedAt: ZonedDateTime
  val deletedAt: Option[ZonedDateTime]
}
