package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.errors.{APIError, Errors}
import com.github.makiftutuncu.dreamtheater.services.{SessionService, UserService}
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import com.github.makiftutuncu.dreamtheater.utilities.{ActionUtils, Context, Maybe, UserContext}
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Reads}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scala.util.control.NonFatal

abstract class Controller(userService: UserService,
                          sessionService: SessionService,
                          cc: ControllerComponents) extends AbstractController(cc) with ActionUtils {
  def publicAction(f: Context[AnyContent] => Future[(JsValue, Result)])(implicit ec: ExecutionContext): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      val context = new Context(request)
      action[AnyContent, Context](context) { ctx =>
        f(ctx).map(Maybe.value)
      }
    }

  def publicAction[A](f: Context[A] => FM[(JsValue, Result)])(implicit ec: ExecutionContext, r: Reads[A]): Action[A] =
    Action.async(parse.json[A]) { request: Request[A] =>
      val context = new Context(request)
      action[A, Context](context) { ctx =>
        f(ctx)
      }
    }

  def privateAction(f: UserContext[AnyContent] => Future[(JsValue, Result)])(implicit ec: ExecutionContext): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      getUserContext(request).flatMap {
        case Left(error) =>
          Future.successful(fail(error))

        case Right(context) =>
          action[AnyContent, UserContext](context) { ctx =>
            f(ctx).map(Maybe.value)
          }
      }
    }

  def privateAction[A](f: UserContext[A] => FM[(JsValue, Result)])(implicit ec: ExecutionContext, r: Reads[A]): Action[A] =
    Action.async(parse.json[A]) { request: Request[A] =>
      getUserContext(request).flatMap {
        case Left(error) =>
          Future.successful(fail(error))

        case Right(context) =>
          action[A, UserContext](context) { ctx =>
            f(ctx)
          }
      }
    }

  private def action[A, C[_] <: Context[_]](ctx: C[A])(f: C[A] => FM[(JsValue, Result)])(implicit ec: ExecutionContext): Future[Result] = {
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

  private def getUserContext[A](request: Request[A])(implicit ec: ExecutionContext): FM[UserContext[A]] =
    request.headers.get(HeaderNames.AUTHORIZATION) match {
      case None =>
        Maybe.errorF(Errors.unauthorized("Authorization header is missing!"))

      case Some(h) if !h.startsWith("Bearer ") =>
        Maybe.errorF(Errors.unauthorized("Authorization header is invalid!"))

      case Some(header) =>
        Maybe.flatMapF(sessionService.getByToken(header.drop(7))) {
          case None =>
            Maybe.errorF(Errors.unauthorized("Token is invalid!"))

          case Some(session) =>
            Maybe.transformF(userService.getById(session.userId)) {
              case None => Maybe.error(Errors.unauthorized("Token is invalid!"))
              case Some(user) => Maybe.value(new UserContext[A](request, user, session))
            }
        }
    }

  private def addHeaders[A, C[_] <: Context[_]](ctx: C[A], result: Result): Result =
    result.withHeaders(
      Context.REQUEST_ID_HEADER_NAME -> ctx.requestId
    )
}
