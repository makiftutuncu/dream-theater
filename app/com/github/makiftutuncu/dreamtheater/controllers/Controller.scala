package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.errors.APIError
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import com.github.makiftutuncu.dreamtheater.utilities.{ActionUtils, Context, Maybe}
import play.api.libs.json.{JsValue, Reads}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

abstract class Controller(cc: ControllerComponents) extends AbstractController(cc) with ActionUtils {
  def PublicAction(f: Context[AnyContent] => Future[(JsValue, Result)])(implicit ec: ExecutionContext): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      action(new Context(request)) { ctx =>
        f(ctx).map(Maybe.value)
      }
    }

  def PublicAction[A](f: Context[A] => FM[(JsValue, Result)])(implicit ec: ExecutionContext, r: Reads[A]): Action[A] =
    Action.async(parse.json[A]) { request: Request[A] =>
      action(new Context(request)) { ctx =>
        f(ctx)
      }
    }

  private def action[A](ctx: Context[A])(f: Context[A] => FM[(JsValue, Result)])(implicit ec: ExecutionContext): Future[Result] = {
    def complete(json: JsValue, result: Result): Result = {
      val finalResult = addHeaders(ctx, addHeaders(ctx, result))
      logResponse(ctx, json, finalResult)
      finalResult
    }

    logRequest(ctx)

    f(ctx).map {
      case Left(apiError)        => complete(apiError.asJson, fail(apiError))
      case Right((json, result)) => complete(json, result)
    }.recover {
      case NonFatal(t) =>
        val apiError = APIError.from(t)
        complete(apiError.asJson, fail(apiError))
    }
  }

  private def addHeaders[A](ctx: Context[A], result: Result): Result =
    result.withHeaders(
      Context.REQUEST_ID_HEADER_NAME -> ctx.requestId
    )
}
