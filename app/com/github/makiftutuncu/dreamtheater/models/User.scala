package com.github.makiftutuncu.dreamtheater.models

import java.time.{LocalDate, ZonedDateTime}
import java.util.UUID

import anorm.{RowParser, SqlMappingError, Success}
import play.api.libs.json.{JsObject, Json, Writes}

import scala.util.Try

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

  override def toString: String = Json.toJson(this).toString
}

object User {
  implicit val userWrites: Writes[User] =
    Json.writes[User].transform { json: JsObject =>
      json - "password" - "salt" - "deletedAt"
    }

  implicit val userRowParser: RowParser[User] =
    RowParser[User] { row =>
      Try {
        val user = User(
          row[UUID]("id"),
          row[String]("email"),
          row[String]("password"),
          row[String]("salt"),
          UserRole.from(row[Int]("user_role")),
          UserType.from(row[Int]("user_type")),
          row[Option[String]]("first_name"),
          row[Option[String]]("last_name"),
          row[Option[Int]]("gender").map(Gender.from),
          row[Option[LocalDate]]("birth_date"),
          row[ZonedDateTime]("created_at"),
          row[ZonedDateTime]("updated_at"),
          row[Option[ZonedDateTime]]("deleted_at")
        )

        Success(user)
      }.fold(
        t => anorm.Error(SqlMappingError(t.getMessage)),
        identity
      )
    }
}
