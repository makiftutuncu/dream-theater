package dev.akif.dreamtheater.user

import java.time.ZoneOffset
import java.util.UUID

import anorm._
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.Errors
import dev.akif.dreamtheater.common.base.Repository
import play.api.db.Database

class UserRepository(db: Database) extends Repository(db) {
  def getById(id: UUID): Z[Option[User]] =
    withDB { implicit connection =>
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

      sql.executeQuery().as(User.userRowParser.singleOpt)
    } {
      case t => Errors.database(s"Cannot get user by id '$id'", t)
    }

  def getByEmail(email: String): Z[Option[User]] =
    withDB { implicit connection =>
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

      sql.executeQuery().as(User.userRowParser.singleOpt)
    } {
      case t => Errors.database(s"Cannot get user by email '$email'", t)
    }

  def insert(user: User): Z[User] =
    withDB { implicit connection =>
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
    } {
      case t => Errors.database(s"Cannot insert user '${user.email}'", t)
    }
}
