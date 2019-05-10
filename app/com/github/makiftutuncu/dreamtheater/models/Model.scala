package com.github.makiftutuncu.dreamtheater.models

import java.time.ZonedDateTime
import java.util.UUID

trait Model {
  val id: UUID
  val createdAt: ZonedDateTime
  val updatedAt: ZonedDateTime
  val deletedAt: Option[ZonedDateTime]
}
