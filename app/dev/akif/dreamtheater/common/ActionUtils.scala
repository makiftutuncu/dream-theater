package dev.akif.dreamtheater.common

import akka.stream.Materializer
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.auth.Ctx
import dev.akif.e.E
import play.api.Logging
import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.mvc.Results.Status
import play.api.mvc._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.util.Try

trait ActionUtils extends Logging {
  implicit val m: Materializer
  implicit val ec: ExecutionContext

  implicit val anyContentReads: Reads[AnyContent] = Reads.pure(AnyContentAsEmpty)

  def fail(e: E, requestId: String): Result = withRequestId(Status(e.code)(e.toString).as(ContentTypes.JSON), requestId)

  def succeed[A: Writes](ctx: Ctx[_], value: A, status: Status = Results.Ok): Result = withRequestId(status(Json.toJson(value)), ctx.requestId)

  def logRequestSuccess[I](request: RequestHeader, requestId: String, in: I): Unit = {
    val log = getRequestLog(request, requestId, in.toString)
    logger.info(log)
  }

  def logRequestError(request: RequestHeader, requestId: String, e: E): Unit = {
    val log = getRequestLog(request, requestId, e.data.getOrDefault("body", ""))
    logger.error(log)
  }

  def logResponseSuccess[O](statusCode: Int, request: RequestHeader, requestId: String, requestTime: Long, out: O): Unit =
    out match {
      case res: Result =>
        val futureBody = res.body.dataStream.runFold(new StringBuilder) {
          case (sb, bs) =>
            sb.append(new String(bs.toArray))
        }

        futureBody.foreach { sb =>
          logResponseSuccess(statusCode, request, requestId, requestTime, sb.toString)
        }

      case o =>
        val log = getResponseLog(statusCode, request, requestId, requestTime, o.toString)
        logger.info(log)
    }

  def logResponseError(request: RequestHeader, requestId: String, e: E): Unit = {
    val log = getResponseLog(e.code, request, requestId, System.currentTimeMillis, e.toString)
    logger.error(log)
  }

  def finishRequest[O](request: Request[AnyContent], requestId: String, zio: Z[(Ctx[_], O, Result)]): Z[Result] =
    zio.foldM(
      e => Z.succeed(logResponseError(request, requestId, e)) *> Z.succeed(fail(e, requestId)),
      { case (ctx, out, result) => Z.succeed(logResponseSuccess(result.header.status, request, requestId, ctx.at, out)) *> Z.succeed(withRequestId(result, requestId)) }
    )

  protected def parseJson[I: Reads](request: Request[AnyContent]): Z[I] = {
    bodyAsStringOrJson(request.body) match {
      case Left(body) =>
        Z.fail(Errors.invalidBody.data("body", body))

      case Right(json) =>
        json.validate[I] match {
          case JsError(errors) =>
            Z.fail(
              errors.foldLeft(Errors.invalidBody) {
                case (e, (path, es)) =>
                  e.data(path.path.map(_.toJsonString).mkString, es.flatMap(_.messages).mkString(", "))
              }
            )

          case JsSuccess(in, _) =>
            Z.succeed(in)
        }
    }
  }

  @tailrec
  protected final def bodyAsStringOrJson(body: AnyContent): Either[String, JsValue] =
    body match {
      case AnyContentAsEmpty =>
        Left("")

      case AnyContentAsText(txt) =>
        Left(txt)

      case AnyContentAsFormUrlEncoded(form) =>
        val formAsText = form.flatMap {
          case (k, vs) =>
            vs.map(v => if (Option(v).map(_.trim).getOrElse("").isEmpty) k else s"$k=$v")
        }.mkString("&")
        Left(formAsText)

      case AnyContentAsRaw(raw) =>
        val rawAsText = raw.asBytes().mkString
        Try(Json.parse(rawAsText)).toEither.fold(_ => Left(rawAsText), j => Right(j))

      case AnyContentAsXml(xml) =>
        Left(xml.toString())

      case AnyContentAsMultipartFormData(mfd) =>
        bodyAsStringOrJson(AnyContentAsFormUrlEncoded(mfd.asFormUrlEncoded))

      case AnyContentAsJson(json) =>
        Right(json)
    }

  private def withRequestId(result: Result, requestId: String): Result = result.withHeaders(Ctx.requestIdHeaderName -> requestId)

  private def getRequestLine(request: RequestHeader): String = s"${request.method} ${request.uri}"

  private def getRequestLog(request: RequestHeader, requestId: String, in: String): String = {
    val sb = new StringBuilder(s"Incoming Request\n")
    appendForRequest(sb, getRequestLine(request))
    appendForRequest(sb, s"${Ctx.requestIdHeaderName}: $requestId")
    request.headers.toMap.filterNot(_._1 == Ctx.requestIdHeaderName).foreachEntry((name, values) => values.foreach(value => appendForRequest(sb, s"$name: $value")))
    if (in.nonEmpty) {
      appendForRequest(sb, "")
      appendForRequest(sb, in)
    }
    sb.toString()
  }

  private def getResponseLog(statusCode: Int, request: RequestHeader, requestId: String, requestTime: Long, out: String): String = {
    val sb = new StringBuilder(s"Outgoing Response\n")
    appendForResponse(sb, s"$statusCode ${getRequestLine(request)}")
    appendForResponse(sb, s"${Ctx.requestIdHeaderName}: $requestId")
    appendForResponse(sb, s"Took: ${System.currentTimeMillis - requestTime} ms")
    if (out.nonEmpty) {
      appendForResponse(sb, "")
      appendForResponse(sb, out)
    }
    sb.toString()
  }

  private def appendForRequest(sb: StringBuilder, what: String): StringBuilder  = append(sb, "< ", what)
  private def appendForResponse(sb: StringBuilder, what: String): StringBuilder = append(sb, "> ", what)

  private def append(sb: StringBuilder, prefix: String, what: String): StringBuilder = sb.append(prefix).append(what).append("\n")
}
