package dev.akif.dreamtheater.common

import dev.akif.e.E
import org.postgresql.util.PSQLException
import play.api.http.Status

object Errors {
  lazy val e400: E = E.of(Status.BAD_REQUEST,           "bad-request")
  lazy val e401: E = E.of(Status.UNAUTHORIZED,          "unauthorized")
  lazy val e404: E = E.of(Status.NOT_FOUND,             "not-found")
  lazy val e500: E = E.of(Status.INTERNAL_SERVER_ERROR, "internal-server-error")

  lazy val invalidBody: E = e400.name("invalid-body")

  def unauthorized(message: String): E = e400.name("unauthorized").message(message)

  lazy val invalidLogin: E = e401.name("unauthorized").message("Invalid login credentials")

  def database(message: String): E = e500.name("database").message(message)

  lazy val unknown: E = e500.name("unknown")

  def byStatusCode(statusCode: Int): E =
    statusCode match {
      case Status.BAD_REQUEST           => e400
      case Status.UNAUTHORIZED          => e401
      case Status.NOT_FOUND             => e404
      case Status.INTERNAL_SERVER_ERROR => e404
      case _                            => unknown
    }

  object PSQL {
    object UniqueKeyInsert {
      private val regex = "Key \\((.+)\\)=\\((.+)\\) already exists".r

      def unapply(e: PSQLException): Option[(String, String)] =
        regex.findFirstMatchIn(e.getMessage).flatMap { m =>
          val matches = m.subgroups

          for {
            column <- matches.headOption
            value  <- matches.lastOption
          } yield {
            column -> value
          }
        }
    }

    object ForeignKeyInsert {
      private val regex = "Key \\((.+)\\)=\\((.+)\\) is not present in table \"(.+)\"".r

      def unapply(e: PSQLException): Option[(String, String, String)] =
        regex.findFirstMatchIn(e.getMessage).flatMap { m =>
          val matches = m.subgroups

          for {
            column <- matches.headOption
            value  <- matches.drop(1).headOption
            table  <- matches.lastOption
          } yield {
            (column, value, table)
          }
        }
    }

    object ForeignKeyDelete {
      private val regex = "Key \\((.+)\\)=\\((.+)\\) is still referenced from table \"(.+)\"".r

      def unapply(e: PSQLException): Option[(String, String, String)] =
        regex.findFirstMatchIn(e.getMessage).flatMap { m =>
          val matches = m.subgroups

          for {
            column <- matches.headOption
            value  <- matches.drop(1).headOption
            table  <- matches.lastOption
          } yield {
            (column, value, table)
          }
        }
    }
  }
}
