package dev.akif.dreamtheater.dream

import akka.stream.Materializer
import dev.akif.dreamtheater.auth.UserCtx
import dev.akif.dreamtheater.common.UserActions
import dev.akif.dreamtheater.common.base.Controller
import dev.akif.dreamtheater.session.SessionService
import dev.akif.dreamtheater.user.UserService
import play.api.mvc.{Action, AnyContent, ControllerComponents, Results}
import zio.Runtime

import scala.concurrent.ExecutionContext

class DreamController(dreamService: DreamService,
                      override val userService: UserService,
                      override val sessionService: SessionService,
                      cc: ControllerComponents)(override implicit val runtime: Runtime[_],
                                                override implicit val m: Materializer,
                                                override implicit val ec: ExecutionContext) extends Controller(cc)
                                                                                               with UserActions {
  def getAll(page: Option[Int], pageCount: Option[Int]): Action[AnyContent] =
    userActionReturning[List[Dream]] { ctx: UserCtx[AnyContent] =>
      dreamService.getDreams(ctx.user.id, page.getOrElse(0), pageCount.getOrElse(1))
    }

  val create: Action[AnyContent] =
    userActionParsing[CreateDreamRequest] { ctx: UserCtx[CreateDreamRequest] =>
      dreamService.create(ctx.user.id, ctx.in).map { dream =>
        succeed(ctx, dream, Results.Created)
      }
    }
}
