package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.models.Session
import com.github.makiftutuncu.dreamtheater.services.{SessionService, UserService}
import com.github.makiftutuncu.dreamtheater.utilities.{Context, Maybe, UserContext}
import com.github.makiftutuncu.dreamtheater.views.{LoginUserRequest, RegisterUserRequest}
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(userService: UserService,
                               sessionService: SessionService,
                               cc: ControllerComponents) extends Controller(userService, sessionService, cc) {
  val register: Action[RegisterUserRequest] =
    publicAction { implicit ctx: Context[RegisterUserRequest] =>
      Maybe.mapF(userService.register(ctx.body)) {
        case (user, session) =>
          val json   = Json.toJson(user)
          val result = succeed(json).withHeaders(Session.AUTH_TOKEN_HEADER_NAME -> session.token)

          json -> result
      }
    }

  val login: Action[LoginUserRequest] =
    publicAction { implicit ctx: Context[LoginUserRequest] =>
      Maybe.mapF(userService.login(ctx.body)) {
        case (user, session) =>
          val json   = Json.toJson(user)
          val result = succeed(json).withHeaders(Session.AUTH_TOKEN_HEADER_NAME -> session.token)

          json -> result
      }
    }

  val me: Action[AnyContent] =
    privateAction { implicit ctx: UserContext[AnyContent] =>
      val json   = Json.toJson(ctx.user)
      val result = succeed(json)

      Future.successful(json -> result)
    }
}
