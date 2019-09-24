package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.services.{SessionService, UserService}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}

@Singleton
class HomeController @Inject()(userService: UserService,
                               sessionService: SessionService,
                               cc: ControllerComponents) extends Controller(userService, sessionService, cc) {
  val home: Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      Ok
    }

  val ping: Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      Ok("pong")
    }
}
