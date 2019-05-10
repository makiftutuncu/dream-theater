package com.github.makiftutuncu.dreamtheater.controllers

import javax.inject._
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  val home: Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      Ok("Dream Theater is running!")
    }
}
