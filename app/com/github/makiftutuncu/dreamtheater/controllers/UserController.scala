package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.views.{LoginUserRequest, RegisterUserRequest}
import com.github.makiftutuncu.dreamtheater.models.User
import com.github.makiftutuncu.dreamtheater.services.UserService
import com.github.makiftutuncu.dreamtheater.utilities.Context
import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(userService: UserService,
                               cc: ControllerComponents) extends Controller(cc) {
  val register: Action[RegisterUserRequest] =
    PublicAction[RegisterUserRequest, User] { implicit ctx: Context[RegisterUserRequest] =>
      userService.register(ctx.body)
    }

  val login: Action[LoginUserRequest] =
    PublicAction[LoginUserRequest, User] { implicit ctx: Context[LoginUserRequest] =>
      userService.login(ctx.body).map(_.map(_._1))
    }
}
