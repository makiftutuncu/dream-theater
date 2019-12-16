package dev.akif.dreamtheater.user

import java.sql.Connection
import java.time.ZoneOffset
import java.util.UUID

import anorm._
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.Errors
import dev.akif.dreamtheater.common.Errors.PSQL
import dev.akif.dreamtheater.common.base.Repository

class UserRepository extends Repository {
  def getById(id: UUID)(implicit connection: Connection): Z[Option[User]] =
    Z {
      SQL(
        """
          |SELECT id, email, password, salt, first_name, last_name, gender, birth_date, created_at, updated_at, deleted_at
          |FROM users
          |WHERE id = {id}::uuid AND deleted_at IS NULL
        """.stripMargin
      )
        .on(NamedParameter("id", id))
        .executeQuery()
        .as(User.userRowParser.singleOpt)
    }

  def getByEmail(email: String)(implicit connection: Connection): Z[Option[User]] =
    Z {
      SQL(
        """
          |SELECT id, email, password, salt, first_name, last_name, gender, birth_date, created_at, updated_at, deleted_at
          |FROM users
          |WHERE email = {email} AND deleted_at IS NULL
        """.stripMargin
      )
        .on(NamedParameter("email", email))
        .executeQuery()
        .as(User.userRowParser.singleOpt)
    }

  def insert(user: User)(implicit connection: Connection): Z[User] =
    Z.applyHandlingErrors {
      SQL(
        """
          |INSERT INTO users(id, email, password, salt, first_name, last_name, gender, birth_date, created_at, updated_at, deleted_at)
          |VALUES({id}::uuid, {email}, {password}, {salt}, {firstName}, {lastName}, {gender}, {birthDate}, {createdAt}, {updatedAt}, {deletedAt})
        """.stripMargin
      )
        .on(
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
        .executeUpdate()

      user
    } {
      case PSQL.UniqueKeyInsert(column, value) =>
        Errors.database("Cannot insert user").data("reason", s"'$value' as '$column' is already used!")
    }
}
