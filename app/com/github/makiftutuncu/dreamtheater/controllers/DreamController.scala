package com.github.makiftutuncu.dreamtheater.controllers

import com.github.makiftutuncu.dreamtheater.services.{DreamService, SessionService, UserService}
import com.github.makiftutuncu.dreamtheater.utilities.{Maybe, UserContext}
import com.github.makiftutuncu.dreamtheater.views.CreateDreamRequest
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DreamController @Inject()(dreamService: DreamService,
                                userService: UserService,
                                sessionService: SessionService,
                                cc: ControllerComponents) extends Controller(userService, sessionService, cc) {
  def getAll(page: Option[Int], pageCount: Option[Int]): Action[AnyContent] =
    privateAction { implicit ctx: UserContext[AnyContent] =>
      Maybe.mapF(dreamService.getDreams(ctx.user.id, page.getOrElse(0), pageCount.getOrElse(1))) { dreams =>
        val json   = Json.toJson(dreams)
        val result = succeed(json)

        json -> result
      }
    }

  val create: Action[CreateDreamRequest] =
    privateActionWithBody { implicit ctx: UserContext[CreateDreamRequest] =>
      Maybe.mapF(dreamService.create(ctx.user.id, ctx.body)) { dream =>
        val json   = Json.toJson(dream)
        val result = succeed(json, Results.Created)

        json -> result
      }
    }
}
