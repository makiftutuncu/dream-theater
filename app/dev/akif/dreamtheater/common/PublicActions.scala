package dev.akif.dreamtheater.common

import akka.stream.Materializer
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.auth.Ctx
import dev.akif.dreamtheater.common.base.Controller
import play.api.libs.json.{Reads, Writes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

trait PublicActions extends ActionUtils { self: Controller =>
  implicit val m: Materializer
  implicit val ec: ExecutionContext

  def publicAction(action: Ctx[AnyContent] => Z[Result]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      publicAction[Result](request, action, { result: Result => result })
    }

  def publicActionReturning[O: Writes](action: Ctx[AnyContent] => Z[O]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      publicAction[O](request, action, { out: O => asJson(out) })
    }

  def publicActionParsing[I: Reads](action: Ctx[I] => Z[Result]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      publicActionParsing[I, Result](request, action, { result: Result => result })
    }

  def publicActionParsingAndReturning[I: Reads, O: Writes](action: Ctx[I] => Z[O]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      publicActionParsing[I, O](request, action, { out: O => asJson(out) })
    }

  private def publicAction[O](request: Request[AnyContent],
                              action: Ctx[AnyContent] => Z[O],
                              toResult: O => Result): Future[Result] =
    zioToFuture {
      val requestId = Ctx.getOrCreateRequestId(request)
      val ctx       = new Ctx[AnyContent](request, AnyContentAsEmpty, requestId)

      val zio = for {
        _      <- Z.succeed(logRequestSuccess(request, requestId, ""))
        out    <- action(ctx)
        result  = toResult(out)
      } yield {
        (ctx, out, result)
      }

      finishRequest(request, requestId, zio)
    }

  private def publicActionParsing[I: Reads, O](request: Request[AnyContent],
                                               action: Ctx[I] => Z[O],
                                               toResult: O => Result): Future[Result] =
    zioToFuture {
      val requestId = Ctx.getOrCreateRequestId(request)

      val zio = for {
        in     <- parseJson[I](request).mapError { e => logRequestError(request, requestId, e); e }
        ctx     = new Ctx[I](request, in, requestId)
        _      <- Z.succeed(logRequestSuccess(request, requestId, in))
        out    <- action(ctx)
        result  = toResult(out)
      } yield {
        (ctx, out, result)
      }

      finishRequest(request, requestId, zio)
    }
}
