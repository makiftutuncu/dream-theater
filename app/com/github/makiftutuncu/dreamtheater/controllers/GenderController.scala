package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.errors.E
import com.github.makiftutuncu.dreamtheater.errors.E.Code
import com.github.makiftutuncu.dreamtheater.models.Gender
import com.github.makiftutuncu.dreamtheater.services.GenderService
import com.github.makiftutuncu.dreamtheater.utilities.Context
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import zio.{Runtime, ZIO}
import zio.internal.PlatformLive

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class GenderController @Inject()(genderService: GenderService, cc: ControllerComponents) extends ZController(cc) {
  implicit val runtime: Runtime[Any] = Runtime[Any]("", PlatformLive.Default)

  val getAll: Action[AnyContent] =
    public[AnyContent, List[Gender]](parse.anyContent) { ctx: Context[AnyContent] =>
      genderService.getAll
    }

  def get(key: String): Action[AnyContent] =
    public[AnyContent, Gender](parse.anyContent) { ctx: Context[AnyContent] =>
      genderService.get(key)
    }
}
