package com.github.makiftutuncu.dreamtheater.utilities

import java.util.UUID

import play.api.mvc.{Request, WrappedRequest}

class Context[T](val request: Request[T]) extends WrappedRequest[T](request) {
  val requestId: String = request.headers.get(Context.REQUEST_ID_HEADER_NAME).getOrElse(UUID.randomUUID.toString)
}

object Context {
  val REQUEST_ID_HEADER_NAME = "X-Request-Id"
}
