package com.github.makiftutuncu.dreamtheater.repositories

import java.time.ZoneOffset

import anorm._
import com.github.makiftutuncu.dreamtheater.errors.APIError
import com.github.makiftutuncu.dreamtheater.models.User
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.db.Database
import play.api.http.Status
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class UserRepository @Inject()(db: Database) extends Repository(db) with Logging {
  def getByEmail(email: String)(implicit ec: ExecutionContext): Future[Either[APIError, Option[User]]] =
    Future {
      try {
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

        db.withConnection { implicit connection =>
          Right(sql.executeQuery().as(User.userRowParser.singleOpt))
        }
      } catch {
        case NonFatal(t) =>
          val error = APIError(Status.INTERNAL_SERVER_ERROR, s"Cannot get user by email '$email': ${t.getMessage}", Json.obj(), Some(t))
          logger.error(error.message, error)
          Left(error)
      }
    }

  def insert(user: User)(implicit ec: ExecutionContext): Future[Either[APIError, User]] =
    Future {
      try {
        val sql =
          SQL(
            """
              |INSERT INTO users(id, email, password, salt, user_role, user_type, first_name, last_name, gender, birth_date, created_at, updated_at, deleted_at)
              |VALUES({id}::uuid, {email}, {password}, {salt}, {userRole}, {userType}, {firstName}, {lastName}, {gender}, {birthDate}, {createdAt}, {updatedAt}, {deletedAt})
            """.stripMargin
          ).on(
            NamedParameter("id",         user.id),
            NamedParameter("email",      user.email),
            NamedParameter("password",   user.password),
            NamedParameter("salt",       user.salt),
            NamedParameter("userRole",   user.userRole.index),
            NamedParameter("userType",   user.userType.index),
            NamedParameter("firstName",  user.firstName),
            NamedParameter("lastName",   user.lastName),
            NamedParameter("gender",     user.gender.map(_.index)),
            NamedParameter("birthDate",  user.birthDate.map(_.atStartOfDay(ZoneOffset.UTC))),
            NamedParameter("createdAt",  user.createdAt),
            NamedParameter("updatedAt",  user.updatedAt),
            NamedParameter("deletedAt",  user.deletedAt),
        )

        db.withConnection { implicit connection =>
          sql.executeUpdate()
        }

        Right(user)
      } catch {
        case NonFatal(t) =>
          val error = APIError(Status.INTERNAL_SERVER_ERROR, s"Cannot insert user '${user.email}': ${t.getMessage}", Json.obj(), Some(t))
          logger.error(error.message, error)
          Left(error)
      }
    }
}
