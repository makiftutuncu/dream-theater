package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.services.{SessionService, UserService}
import com.github.makiftutuncu.dreamtheater.utilities.Context
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HomeController @Inject()(userService: UserService,
                               sessionService: SessionService,
                               cc: ControllerComponents) extends Controller(userService, sessionService, cc) {
  val home: Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      Ok
    }

  val ping: Action[AnyContent] =
    publicAction { implicit ctx: Context[AnyContent] =>
      val body = "pong"
      Future.successful(Json.toJson(body) -> succeed(body))
    }
}
