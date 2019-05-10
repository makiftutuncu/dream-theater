package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.errors.APIError
import com.github.makiftutuncu.dreamtheater.utilities.{ActionUtils, Context}
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

abstract class Controller(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) with ActionUtils {
  def LoggingAction[A: Writes](f: Context[AnyContent] => Future[A]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      action(new Context(request))(f)
    }

  def LoggingAction[A: Reads, B: Writes](f: Context[A] => Future[B]): Action[A] =
    Action.async(parse.json[A]) { request: Request[A] =>
      action(new Context(request))(f)
    }

  private def action[A, B: Writes](ctx: Context[A])(f: Context[A] => Future[B]): Future[Result] = {
    logRequest(ctx)

    f(ctx).map { response: B =>
      val json = Json.toJson(response)
      val result = addHeaders(ctx, succeed(json))

      logResponse(ctx, json, result)

      result
    }.recover {
      case t: Throwable =>
        val apiError = APIError.from(t)
        val json = Json.toJson(apiError)
        val result = addHeaders(ctx, fail(apiError))

        logResponse(ctx, json, result)

        result
    }
  }

  private def addHeaders[A](ctx: Context[A], result: Result): Result =
    result.withHeaders(
      Context.REQUEST_ID_HEADER_NAME -> ctx.requestId
    )
}
