package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.models.Session
import com.github.makiftutuncu.dreamtheater.services.{SessionService, UserService}
import com.github.makiftutuncu.dreamtheater.utilities.{Context, Maybe, UserContext}
import com.github.makiftutuncu.dreamtheater.views.{LoginUserRequest, RegisterUserRequest}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(userService: UserService,
                               sessionService: SessionService,
                               cc: ControllerComponents) extends Controller(userService, sessionService, cc) {
  val register: Action[RegisterUserRequest] =
    publicActionWithBody { implicit ctx: Context[RegisterUserRequest] =>
      Maybe.mapF(userService.register(ctx.body)) {
        case (user, session) =>
          val json   = Json.toJson(user)
          val result = succeed(json).withHeaders(Session.AUTH_TOKEN_HEADER_NAME -> session.token)

          json -> result
      }
    }

  val login: Action[LoginUserRequest] =
    publicActionWithBody { implicit ctx: Context[LoginUserRequest] =>
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

      Maybe.valueF(json -> result)
    }
}
