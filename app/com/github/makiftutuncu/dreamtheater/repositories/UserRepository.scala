package com.github.makiftutuncu.dreamtheater.repositories

import java.time.ZoneOffset
import java.util.UUID

import anorm._
import com.github.makiftutuncu.dreamtheater.errors.Errors
import com.github.makiftutuncu.dreamtheater.models.User
import com.github.makiftutuncu.dreamtheater.models.User.userRowParser
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import javax.inject.{Inject, Singleton}
import play.api.db.Database

import scala.concurrent.ExecutionContext

@Singleton
class UserRepository @Inject()(db: Database) extends Repository(db) {
  def getById(id: UUID)(implicit ec: ExecutionContext): FM[Option[User]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, email, password, salt, first_name, last_name, gender, birth_date, created_at, updated_at, deleted_at
            |FROM users
            |WHERE id = {id}::uuid AND deleted_at IS NULL
          """.stripMargin
        ).on(
          NamedParameter("id", id)
        )

      sql.executeQuery().as(userRowParser.singleOpt)
    } { throwable =>
      Errors.database(s"Cannot get user by id '$id'", throwable)
    }

  def getByEmail(email: String)(implicit ec: ExecutionContext): FM[Option[User]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, email, password, salt, first_name, last_name, gender, birth_date, created_at, updated_at, deleted_at
            |FROM users
            |WHERE email = {email} AND deleted_at IS NULL
          """.stripMargin
        ).on(
          NamedParameter("email", email)
        )

      sql.executeQuery().as(userRowParser.singleOpt)
    } { throwable =>
      Errors.database(s"Cannot get user by email '$email'", throwable)
    }

  def insert(user: User)(implicit ec: ExecutionContext): FM[User] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |INSERT INTO users(id, email, password, salt, first_name, last_name, gender, birth_date, created_at, updated_at, deleted_at)
            |VALUES({id}::uuid, {email}, {password}, {salt}, {firstName}, {lastName}, {gender}, {birthDate}, {createdAt}, {updatedAt}, {deletedAt})
          """.stripMargin
        ).on(
          NamedParameter("id",         user.id),
          NamedParameter("email",      user.email),
          NamedParameter("password",   user.password),
          NamedParameter("salt",       user.salt),
          NamedParameter("firstName",  user.firstName),
          NamedParameter("lastName",   user.lastName),
          NamedParameter("gender",     user.gender.map(_.key)),
          NamedParameter("birthDate",  user.birthDate.map(_.atStartOfDay(ZoneOffset.UTC))),
          NamedParameter("createdAt",  user.createdAt),
          NamedParameter("updatedAt",  user.updatedAt),
          NamedParameter("deletedAt",  user.deletedAt),
      )

      sql.executeUpdate()

      user
    } { throwable =>
      Errors.database(s"Cannot insert user '${user.email}'", throwable)
    }
}
