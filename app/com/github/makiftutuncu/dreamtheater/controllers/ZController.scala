package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.Z
import com.github.makiftutuncu.dreamtheater.errors.E
import com.github.makiftutuncu.dreamtheater.utilities.Context
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{AbstractController, Action, BodyParser, ControllerComponents, Request, Result}
import zio.{Cause, Exit, Runtime}
import zio.Exit.{Failure, Success}

import scala.concurrent.{ExecutionContext, Promise}

abstract class ZController(cc: ControllerComponents) extends AbstractController(cc) {
  def public[REQ, RES](parser: BodyParser[REQ])(action: Context[REQ] => Z[RES])(implicit rt: Runtime[Any], w: Writes[RES], ec: ExecutionContext): Action[REQ] =
    Action.async[REQ](parser) { request: Request[REQ] =>
      val ctx = new Context[REQ](request)

      val z = action(ctx)

      val result = Promise[Result]

      rt.unsafeRunAsync(z) {
        case Success(res: RES) =>
          result.success(complete(res, Ok))

        case Failure(cause: Cause[E]) =>
          cause.fold[Unit](
            e => result.success(complete(e)),
            throwable => result.failure(throwable),
            result.failure(new Exception("Interrupted!"))
          )(
            (_, _) => result.failure(new Exception("Then")),
            (_, _) => result.failure(new Exception("Both")),
            (_, _) => result.failure(new Exception("Traced"))
          )
      }

      result.future
    }

  private def complete[RES](res: RES, status: Status)(implicit w: Writes[RES]): Result = status(Json.toJson(res))

  private def complete(e: E): Result = complete[E](e, InternalServerError)
}
