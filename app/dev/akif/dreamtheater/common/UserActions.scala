package dev.akif.dreamtheater.common

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.auth.UserCtx
import dev.akif.dreamtheater.common.base.Controller
import dev.akif.dreamtheater.session.SessionService
import dev.akif.dreamtheater.user.UserService
import play.api.libs.json.{Reads, Writes}
import play.api.mvc._

import scala.concurrent.Future

trait UserActions extends ActionUtils { self: Controller =>
  val sessionService: SessionService
  val userService: UserService

  implicit val anyContentReads: Reads[AnyContent] = Reads.pure(AnyContentAsEmpty)

  def userAction(action: UserCtx[AnyContent] => Z[Result]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      userAction[AnyContent, Result](request, action, { result: Result => result })
    }

  def userAction[O: Writes](action: UserCtx[AnyContent] => Z[O]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      userAction[AnyContent, O](request, action, { out: O => asJson(out) })
    }

  def userActionParsing[I: Reads](action: UserCtx[I] => Z[Result]): Action[I] =
    Action(parseJson[I]).async { request: Request[I] =>
      userAction[I, Result](request, action, { result: Result => result })
    }

  def userActionParsing[I: Reads, O: Writes](action: UserCtx[I] => Z[O]): Action[I] =
    Action(parseJson[I]).async { request: Request[I] =>
      userAction[I, O](request, action, { out: O => asJson(out) })
    }

  private def userAction[I, O](request: Request[I],
                               action: UserCtx[I] => Z[O],
                               toResult: O => Result): Future[Result] =
    zioToFuture {
      for {
        token   <- getBearerToken(request)
        session <- sessionService.getByToken(token)    failIfNone Errors.unauthorized("Invalid session id")
        user    <- userService.getById(session.userId) failIfNone Errors.unauthorized("Invalid user id in session")
        ctx      = new UserCtx(request, user, session)
        out     <- action(ctx)
      } yield {
        withRequestId(toResult(out), ctx)
      }
    }
}
