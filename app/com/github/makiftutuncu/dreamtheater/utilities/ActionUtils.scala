package com.github.makiftutuncu.dreamtheater.utilities

import com.github.makiftutuncu.dreamtheater.errors.APIError
import play.api.Logging
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.Results.Status
import play.api.mvc.{RequestHeader, Result, Results}

import scala.language.higherKinds

trait ActionUtils extends Logging {
  def fail(apiError: APIError): Result = Status(apiError.status)(apiError.asJson)

  def succeed[A: Writes](value: A, status: Status = Results.Ok): Result = status(Json.toJson(value))

  def logRequest[A, C[_] <: Context[_]](ctx: C[A]): Unit = {
    val sb = new StringBuilder(s"Request(${ctx.requestId})\n${ctx.request.method} ${ctx.request.path}")

    appendHeaders(sb, ctx.request.headers.toMap)

    if (ctx.request.hasBody) {
      sb.append("\n").append(ctx.request.body)
    }

    logger.info(sb.toString)
  }

  def logRequest(request: RequestHeader): Unit = {
    val sb = new StringBuilder(s"Request\n${request.method} ${request.path}")

    appendHeaders(sb, request.headers.toMap)

    logger.info(sb.toString)
  }

  def logResponse[A, C[_] <: Context[_]](ctx: C[A], response: JsValue, result: Result): Unit = {
    val sb = new StringBuilder(s"Response(${ctx.requestId})\n${ctx.request.method} ${ctx.request.path}")

    appendHeaders(sb, result.header.headers.mapValues(s => Seq(s)))

    sb.append("\n").append(response)

    logger.info(sb.toString)
  }

  def logResponse(request: RequestHeader, response: JsValue, result: Result): Unit = {
    val sb = new StringBuilder(s"Response\n${request.method} ${request.path}")

    appendHeaders(sb, result.header.headers.mapValues(s => Seq(s)))

    sb.append("\n").append(response)

    logger.info(sb.toString)
  }

  val headersOfInterest: Set[String] =
    Set(
      HeaderNames.USER_AGENT
    )

  val headersToOmit: Set[String] =
    Set(
      HeaderNames.AUTHORIZATION
    )

  private def appendHeaders(sb: StringBuilder, headers: Map[String, Seq[String]]): Unit =
    headers.foreach { case (name, values) =>
      if (headersOfInterest(name) && !headersToOmit(name)) {
        values.foreach(v => sb.append("\n").append(s"$name: $v"))
      }
    }
}
