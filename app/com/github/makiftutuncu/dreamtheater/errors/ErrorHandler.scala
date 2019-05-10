package com.github.makiftutuncu.dreamtheater.errors

import com.github.makiftutuncu.dreamtheater.utilities.ActionUtils
import play.api.http.HttpErrorHandler
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler with ActionUtils {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(fail(APIError(statusCode, message, headerData(request), None)))

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    Future.successful(fail(APIError(INTERNAL_SERVER_ERROR, exception.getMessage, headerData(request), Some(exception))))

  private def headerData(request: RequestHeader): JsValue =
    request.headers.toSimpleMap.foldLeft(Json.obj()) {
      case (json, (header, value)) if headersOfInterest(header) && !headersToOmit(header) => json + (header -> JsString(value))
      case (json, _)                                                                      => json
    }
}
