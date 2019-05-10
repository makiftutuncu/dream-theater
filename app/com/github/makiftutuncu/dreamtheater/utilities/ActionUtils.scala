package com.github.makiftutuncu.dreamtheater.utilities

import com.github.makiftutuncu.dreamtheater.errors.APIError
import play.api.Logging
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.{Ok, Status}

import scala.concurrent.{ExecutionContext, Future}

trait ActionUtils extends Logging {
  def fail(apiError: APIError): Result                          = Status(apiError.status)(Json.toJson(apiError))
  def succeed[A: Writes](value: A, status: Status = Ok): Result = status(Json.toJson(value))

  def result[A: Writes](result: Either[APIError, A], status: Status = Ok): Result =
    result match {
      case Left(apiError) => fail(apiError)
      case Right(value)   => succeed(value)
    }

  def futureResult[A](futureResult: Future[Either[APIError, A]], status: Status = Ok)(implicit ec: ExecutionContext, w: Writes[A]): Future[Result] =
    futureResult.map(either => result(either, status)).recover {
      case t: Throwable => fail(APIError.from(t))
    }

  def logRequest[A](ctx: Context[A]): Unit = {
    val sb = new StringBuilder(s"Request(${ctx.requestId})\n${ctx.request.method} ${ctx.request.path}")

    appendHeaders(sb, ctx.request.headers.toMap)

    if (ctx.request.hasBody) {
      sb.append("\n").append(ctx.request.body)
    }

    logger.debug(sb.toString)
  }

  def logResponse[A](ctx: Context[A], response: JsValue, result: Result): Unit = {
    val sb = new StringBuilder(s"Response(${ctx.requestId})\n${ctx.request.method} ${ctx.request.path}")

    appendHeaders(sb, result.header.headers.mapValues(s => Seq(s)))

    sb.append("\n").append(response)

    logger.debug(sb.toString)
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
