package com.github.makiftutuncu.dreamtheater.utilities

import java.util.UUID

import play.api.mvc.{Request, WrappedRequest}

class Context[A](val request: Request[A]) extends WrappedRequest[A](request) {
  val requestId: String = request.headers.get(Context.REQUEST_ID_HEADER_NAME).getOrElse(UUID.randomUUID.toString)
}

object Context {
  val REQUEST_ID_HEADER_NAME = "X-Request-Id"
}
