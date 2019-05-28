package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.errors.APIError
import com.github.makiftutuncu.dreamtheater.utilities.{ActionUtils, Context}
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

abstract class Controller(cc: ControllerComponents) extends AbstractController(cc) with ActionUtils {
  def PublicAction[A](f: Context[AnyContent] => Future[A])(implicit ec: ExecutionContext, w: Writes[A]): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      action(new Context(request))(ctx => f(ctx).map(Right.apply))
    }

  def PublicAction[A, B](f: Context[A] => Future[Either[APIError, B]])(implicit ec: ExecutionContext, r: Reads[A], w: Writes[B]): Action[A] =
    Action.async(parse.json[A]) { request: Request[A] =>
      action(new Context(request))(f)
    }

  private def action[A, B](ctx: Context[A])(f: Context[A] => Future[Either[APIError, B]])(implicit ec: ExecutionContext, w: Writes[B]): Future[Result] = {
    def internalFail(apiError: APIError): Result = {
      val json   = Json.obj("error" -> apiError)
      val result = addHeaders(ctx, fail(apiError))

      logResponse(ctx, json, result)

      result
    }

    def internalSucceed(value: B): Result = {
      val json   = Json.toJson(value)
      val result = addHeaders(ctx, succeed(json))

      logResponse(ctx, json, result)

      result
    }

    logRequest(ctx)

    f(ctx).map {
      case Left(apiError) => internalFail(apiError)
      case Right(value)   => internalSucceed(value)
    }.recover {
      case NonFatal(t) => internalFail(APIError.from(t))
    }
  }

  private def addHeaders[A](ctx: Context[A], result: Result): Result =
    result.withHeaders(
      Context.REQUEST_ID_HEADER_NAME -> ctx.requestId
    )
}
