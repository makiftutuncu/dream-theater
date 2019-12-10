package dev.akif.dreamtheater

import dev.akif.dreamtheater.common.ActionUtils
import dev.akif.e.E
import play.api.http.HttpErrorHandler
import play.api.mvc.Results.{InternalServerError, Status}
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler with ActionUtils {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(Status(statusCode)(message))

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    exception match {
      case e: E =>
        Future.successful(fail(e))

      case t =>
        Future.successful(InternalServerError(t.getMessage))
    }
}
