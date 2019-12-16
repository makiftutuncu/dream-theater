package dev.akif.dreamtheater.common.base

import akka.stream.Materializer
import dev.akif.dreamtheater.auth.UserCtx
import dev.akif.dreamtheater.common.{ActionUtils, Errors}
import dev.akif.dreamtheater.{Z, ZIOOptionExtensions}
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json._
import play.api.mvc._
import zio.Runtime

import scala.concurrent.{ExecutionContext, Future}

abstract class Controller(cc: ControllerComponents)(implicit val runtime: Runtime[_],
                                                    implicit val m: Materializer,
                                                    implicit val ec: ExecutionContext) extends AbstractController(cc)
                                                                                          with ActionUtils {
  protected def getBearerToken(request: Request[_]): Z[String] =
    for {
      header <- Z.succeed(request.headers.get(HeaderNames.AUTHORIZATION)) failIfNone Errors.unauthorized("Token is missing")
      token  <- if (!header.startsWith("Bearer ")) Z.fail(Errors.unauthorized("Token is not a bearer token")) else Z.succeed(header.drop(7))
    } yield {
      token
    }

  protected def withSessionToken[A](result: Result, token: String): Result = result.withHeaders(UserCtx.sessionTokenHeaderName -> token)

  protected def zioToFuture(zio: Z[Result])(implicit runtime: Runtime[_]): Future[Result] = runtime.unsafeRun(zio.toFuture)

  protected def asJson[A: Writes](a: A, statusCode: Int): Result = Status(statusCode)(Json.toJson(a)).as(ContentTypes.JSON)

  protected def asJson[A: Writes](a: A): Result = asJson(a, OK)
}
