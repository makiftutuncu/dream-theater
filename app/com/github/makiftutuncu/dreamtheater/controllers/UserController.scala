package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.models.Session
import com.github.makiftutuncu.dreamtheater.services.UserService
import com.github.makiftutuncu.dreamtheater.utilities.{Context, Maybe}
import com.github.makiftutuncu.dreamtheater.views.{LoginUserRequest, RegisterUserRequest}
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(userService: UserService,
                               cc: ControllerComponents) extends Controller(cc) {
  val register: Action[RegisterUserRequest] =
    PublicAction { implicit ctx: Context[RegisterUserRequest] =>
      Maybe.mapF(userService.register(ctx.body)) {
        case (user, session) =>
          val json   = Json.toJson(user)
          val result = succeed(json).withHeaders(Session.AUTH_TOKEN_HEADER_NAME -> session.token)

          json -> result
      }
    }

  val login: Action[LoginUserRequest] =
    PublicAction { implicit ctx: Context[LoginUserRequest] =>
      Maybe.mapF(userService.login(ctx.body)) {
        case (user, session) =>
          val json   = Json.toJson(user)
          val result = succeed(json).withHeaders(Session.AUTH_TOKEN_HEADER_NAME -> session.token)

          json -> result
      }
    }
}
