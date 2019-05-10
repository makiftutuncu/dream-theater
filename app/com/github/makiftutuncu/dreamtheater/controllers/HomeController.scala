package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.utilities.Context
import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends Controller(cc) {
  val home: Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      Ok
    }

  val ping: Action[AnyContent] =
    LoggingAction { implicit ctx: Context[AnyContent] =>
      Future.successful("pong")
    }
}
