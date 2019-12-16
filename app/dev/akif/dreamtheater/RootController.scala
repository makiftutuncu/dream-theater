package dev.akif.dreamtheater

import akka.stream.Materializer
import dev.akif.dreamtheater.auth.Ctx
import dev.akif.dreamtheater.common.PublicActions
import dev.akif.dreamtheater.common.base.Controller
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import zio.Runtime

import scala.concurrent.ExecutionContext

class RootController(cc: ControllerComponents)(override implicit val runtime: Runtime[_],
                                               override implicit val m: Materializer,
                                               override implicit val ec: ExecutionContext) extends Controller(cc)
                                                                                              with PublicActions {
  val home: Action[AnyContent] =
    publicAction { _: Ctx[AnyContent] =>
      Z.succeed(Ok)
    }

  val ping: Action[AnyContent] =
    publicAction { _: Ctx[AnyContent] =>
      Z.succeed(Ok("pong"))
    }
}
