package dev.akif.dreamtheater.common

import akka.stream.Materializer
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.auth.{Ctx, UserCtx}
import dev.akif.dreamtheater.common.base.Controller
import dev.akif.dreamtheater.session.SessionService
import dev.akif.dreamtheater.user.UserService
import play.api.libs.json.{Reads, Writes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

trait UserActions extends ActionUtils { self: Controller =>
  implicit val m: Materializer
  implicit val ec: ExecutionContext

  val sessionService: SessionService
  val userService: UserService

  def userAction(action: UserCtx[AnyContent] => Z[Result]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      userAction[Result](request, action, { result: Result => result })
    }

  def userActionReturning[O: Writes](action: UserCtx[AnyContent] => Z[O]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      userAction[O](request, action, { out: O => asJson(out) })
    }

  def userActionParsing[I: Reads](action: UserCtx[I] => Z[Result]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      userActionParsing[I, Result](request, action, { result: Result => result })
    }

  def userActionParsingAndReturning[I: Reads, O: Writes](action: UserCtx[I] => Z[O]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      userActionParsing[I, O](request, action, { out: O => asJson(out) })
    }

  private def userAction[O](request: Request[AnyContent],
                            action: UserCtx[AnyContent] => Z[O],
                            toResult: O => Result): Future[Result] =
    zioToFuture {
      val requestId = Ctx.getOrCreateRequestId(request)

      val zio = for {
        _       <- Z.succeed(logRequestSuccess(request, requestId, ""))
        token   <- getBearerToken(request)
        session <- sessionService.getByToken(token)    failIfNone Errors.unauthorized("Invalid session id")
        user    <- userService.getById(session.userId) failIfNone Errors.unauthorized("Invalid user id in session")
        realCtx  = new UserCtx[AnyContent](request, AnyContentAsEmpty, requestId, user, session)
        out     <- action(realCtx)
        result   = toResult(out)
      } yield {
        (realCtx, out, result)
      }

      finishRequest(request, requestId, zio)
    }

  private def userActionParsing[I: Reads, O](request: Request[AnyContent],
                                             action: UserCtx[I] => Z[O],
                                             toResult: O => Result): Future[Result] =
    zioToFuture {
      val requestId = Ctx.getOrCreateRequestId(request)

      val zio = for {
        in      <- parseJson[I](request).mapError { e => logRequestError(request, requestId, e); e }
        _       <- Z.succeed(logRequestSuccess(request, requestId, in))
        token   <- getBearerToken(request)
        session <- sessionService.getByToken(token)    failIfNone Errors.unauthorized("Invalid session id")
        user    <- userService.getById(session.userId) failIfNone Errors.unauthorized("Invalid user id in session")
        realCtx  = new UserCtx[I](request, in, requestId, user, session)
        out     <- action(realCtx)
        result   = toResult(out)
      } yield {
        (realCtx, out, result)
      }

      finishRequest(request, requestId, zio)
    }
}
