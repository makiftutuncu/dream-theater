package com.github.makiftutuncu.dreamtheater.models

import java.time.{LocalDate, ZonedDateTime}
import java.util.UUID

final case class User(override val id: UUID,
                      email: String,
                      password: String,
                      salt: String,
                      userRole: UserRole,
                      userType: UserType,
                      firstName: Option[String],
                      lastName: Option[String],
                      gender: Option[Gender],
                      birthDate: Option[LocalDate],
                      override val createdAt: ZonedDateTime,
                      override val updatedAt: ZonedDateTime,
                      override val deletedAt: Option[ZonedDateTime]) extends Model {
  lazy val age: Option[Int] = birthDate.map(_.until(LocalDate.now).getYears)
}
