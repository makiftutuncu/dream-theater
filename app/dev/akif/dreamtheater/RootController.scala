package dev.akif.dreamtheater

import dev.akif.dreamtheater.auth.Ctx
import dev.akif.dreamtheater.common.PublicActions
import dev.akif.dreamtheater.common.base.Controller
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import zio.{Runtime, ZIO}

class RootController(cc: ControllerComponents)(override implicit val runtime: Runtime[_]) extends Controller(cc) with PublicActions {
  val home: Action[AnyContent] =
    publicAction { _: Ctx[AnyContent] =>
      ZIO.succeed(Ok)
    }

  val ping: Action[AnyContent] =
    publicAction { _: Ctx[AnyContent] =>
      ZIO.succeed(Ok("pong"))
    }
}
