package dev.akif.dreamtheater.common.base

import java.sql.Connection

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.common.Errors
import dev.akif.e.E
import play.api.Logging
import play.api.db.Database
import zio.{Managed, ZIO}

abstract class Repository(db: Database) extends Logging {
  def withDB[A](action: Connection => A)(errorHandler: PartialFunction[Throwable, E]): Z[A] =
    Managed.makeEffect(db.getConnection())(_.close()).use { connection =>
      ZIO(action(connection))
    }.mapError { cause =>
      errorHandler.applyOrElse[Throwable, E](cause, t => Errors.database("Database operation failed").cause(t))
    }
}
