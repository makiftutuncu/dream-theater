package com.github.makiftutuncu.dreamtheater.repositories

import java.sql.Connection

import com.github.makiftutuncu.dreamtheater.errors.APIError
import com.github.makiftutuncu.dreamtheater.utilities.Maybe
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import play.api.Logging
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

abstract class Repository(db: Database) extends Logging {
  def withConnection[A](action: Connection => A)(handleError: Throwable => APIError)(implicit ec: ExecutionContext): FM[A] =
    Future {
      db.withConnection { connection =>
        Maybe.value(action(connection))
      }
    }.recover {
      case NonFatal(t) =>
        val error = handleError(t)
        logger.error(error.message, error)
        Maybe.error(error)
    }
}
