package dev.akif.dreamtheater

import akka.stream.Materializer
import dev.akif.dreamtheater.auth.Ctx
import dev.akif.dreamtheater.common.{ActionUtils, Errors}
import dev.akif.e.E
import play.api.http.HttpErrorHandler
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

class ErrorHandler(override implicit val m: Materializer, override implicit val ec: ExecutionContext) extends HttpErrorHandler with ActionUtils {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val e = Errors.byStatusCode(statusCode).message(message)
    val requestId = Ctx.getOrCreateRequestId(request)
    logResponseError(request, requestId, e)
    Future.successful(fail(e, requestId))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val requestId = Ctx.getOrCreateRequestId(request)
    exception match {
      case e: E =>
        logResponseError(request, requestId, e)
        Future.successful(fail(e, requestId))

      case t =>
        val e = Errors.unknown.cause(t)
        logResponseError(request, requestId, e)
        Future.successful(fail(e, requestId))
    }
  }
}
