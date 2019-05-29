package com.github.makiftutuncu.dreamtheater.repositories

import java.time.ZoneOffset

import anorm._
import com.github.makiftutuncu.dreamtheater.errors.{APIError, Errors}
import com.github.makiftutuncu.dreamtheater.models.User
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class UserRepository @Inject()(db: Database) extends Repository(db) with Logging {
  def getByEmail(email: String)(implicit ec: ExecutionContext): Future[Either[APIError, Option[User]]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT *
            |FROM users
            |WHERE email = {email} AND deleted_at IS NULL
          """.stripMargin
        ).on(
          NamedParameter("email", email)
        )

      Right(sql.executeQuery().as(User.userRowParser.singleOpt))
    }.recover {
      case NonFatal(t) =>
        val error = Errors.database(s"Cannot get user by email '$email'", t)
        logger.error(error.message, error)
        Left(error)
    }

  def insert(user: User)(implicit ec: ExecutionContext): Future[Either[APIError, User]] =
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

      Right(user)
    }.recover {
      case NonFatal(t) =>
        val error = Errors.database(s"Cannot insert user '${user.email}'", t)
        logger.error(error.message, error)
        Left(error)
    }
}
