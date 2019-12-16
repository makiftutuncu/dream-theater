package dev.akif.dreamtheater.common.base

import java.sql.Connection

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.Errors
import dev.akif.e.E
import play.api.Logging
import play.api.db.Database
import zio.Managed

import scala.util.control.NonFatal

abstract class Service(db: Database) extends Logging {
  def withDBHandlingErrors[A](action: Connection => Z[A])(errorHandler: PartialFunction[Throwable, E]): Z[A] =
    Managed
      .makeEffect(db.getConnection())(_.close())
      .mapError[E](cause => handleError(errorHandler, cause, "operation"))
      .use { connection =>
        action(connection)
      }
      .mapError { cause =>
        handleError(errorHandler, cause, "operation")
      }

  def withDBTransactionHandlingErrors[A](action: Connection => Z[A])(errorHandler: PartialFunction[Throwable, E]): Z[A] =
    Managed
      .makeEffect(db.getConnection(false))(_.close())
      .mapError[E](cause => handleError(errorHandler, cause, "transaction"))
      .use { connection =>
        action(connection).foldM(
          cause => Z.succeed(connection.rollback()) *> Z.fail(handleError(errorHandler, cause, "transaction")),
          a     => Z.succeed(connection.commit())   *> Z.succeed(a)
        )
      }

  def withDB[A](action: Connection => Z[A]): Z[A] = withDBHandlingErrors[A](action)(PartialFunction.empty[Throwable, E])

  def withDBTransaction[A](action: Connection => Z[A]): Z[A] = withDBTransactionHandlingErrors[A](action)(PartialFunction.empty[Throwable, E])

  private def handleError(errorHandler: PartialFunction[Throwable, E], t: Throwable, label: String): E =
    errorHandler.applyOrElse[Throwable, E](t, {
      case e: E            => e
      case NonFatal(cause) => Errors.database(s"Database $label failed").cause(cause)
    })
}
