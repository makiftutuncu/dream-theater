package dev.akif.dreamtheater.common.base

import akka.stream.scaladsl.Sink
import akka.util.ByteString
import dev.akif.dreamtheater.{Z, ZIOOptionExtensions}
import dev.akif.dreamtheater.auth.{Ctx, UserCtx}
import dev.akif.dreamtheater.common.{ActionUtils, Errors}
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json._
import play.api.libs.streams.Accumulator
import play.api.mvc._
import zio.Runtime

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class Controller(cc: ControllerComponents)(implicit val runtime: Runtime[_]) extends AbstractController(cc) with ActionUtils {
  protected def getBearerToken(request: Request[_]): Z[String] =
    for {
      header <- Z.succeed(request.headers.get(HeaderNames.AUTHORIZATION)) failIfNone Errors.unauthorized("Token is missing")
      token  <- if (!header.startsWith("Bearer ")) Z.fail(Errors.unauthorized("Token is not a bearer token")) else Z.succeed(header.drop(7))
    } yield {
      token
    }

  protected def withRequestId[A](result: Result, ctx: Ctx[A]): Result = result.withHeaders(Ctx.requestIdHeaderName -> ctx.requestId)

  protected def withSessionToken[A](result: Result, token: String): Result = result.withHeaders(UserCtx.sessionTokenHeaderName -> token)

  protected def zioToFuture(zio: Z[Result])(implicit runtime: Runtime[_]): Future[Result] = runtime.unsafeRun(zio.toFuture)

  protected def asJson[A: Writes](a: A, statusCode: Int): Result = Status(statusCode)(Json.toJson(a)).as(ContentTypes.JSON)

  protected def asJson[A: Writes](a: A): Result = asJson(a, OK)

  protected def parseJson[A: Reads]: BodyParser[A] = {
    def parseBody(bytes: ByteString): Future[Either[Result, A]] =
      Try(Json.parse(bytes.iterator.asInputStream)) match {
        case Failure(parseError) =>
          Future.successful(Left(fail(Errors.invalidBody.cause(parseError))))

        case Success(json) =>
          json.validate[A] match {
            case JsError(failures) =>
              val e = failures.foldLeft(Errors.invalidBody) {
                case (currentE, (path, errors)) =>
                  currentE.data(path.path.map(_.toJsonString).mkString("."), errors.map(_.message).mkString(", "))
              }
              Future.successful(Left(fail(e)))

            case JsSuccess(a, _) =>
              Future.successful(Right(a))
          }
      }

    BodyParser[A] { request: RequestHeader =>
      Accumulator.strict[ByteString, Either[Result, A]](
        _.fold(parseBody(ByteString.empty))(bs => parseBody(bs)),
        Accumulator
          .apply(Sink.fold[ByteString, ByteString](ByteString.empty)((state, bs) => state ++ bs))
          .mapFuture(bs => parseBody(bs))(ExecutionContext.global)
          .toSink
      )
    }
  }
}
