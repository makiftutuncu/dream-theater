package dev.akif.dreamtheater.common

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.auth.Ctx
import dev.akif.dreamtheater.common.base.Controller
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.{Action, AnyContent, Request, Result}

import scala.concurrent.Future

trait PublicActions extends ActionUtils { self: Controller =>
  def publicAction(action: Ctx[AnyContent] => Z[Result]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      publicAction[AnyContent, Result](request, action, { result: Result => result })
    }

  def publicAction[I: Reads](action: Ctx[I] => Z[Result]): Action[I] =
    Action(json[I]).async { request: Request[I] =>
      publicAction[I, Result](request, action, { result: Result => result })
    }

  def publicAction[I: Reads, O: Writes](action: Ctx[I] => Z[O]): Action[I] =
    Action(json[I]).async { request: Request[I] =>
      publicAction[I, O](request, action, { out: O => asJson(out) })
    }

  private def publicAction[I, O](request: Request[I], action: Ctx[I] => Z[O], toResult: O => Result): Future[Result] =
    zioToFuture {
      val ctx = new Ctx(request)
      action(ctx).map { out =>
        withRequestId(toResult(out), ctx)
      }
    }
}
