package dev.akif.dreamtheater.auth

import java.util.UUID

import play.api.mvc.{AnyContent, Request, RequestHeader, WrappedRequest}

class Ctx[+A](val request: Request[AnyContent],
              val in: A,
              val requestId: String) extends WrappedRequest[AnyContent](request) {
  val at: Long = System.currentTimeMillis
}

object Ctx {
  val requestIdHeaderName: String = "X-Request-Id"

  def getOrCreateRequestId(request: RequestHeader): String = request.headers.get(requestIdHeaderName).getOrElse(UUID.randomUUID.toString)
}
