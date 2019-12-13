package dev.akif.dreamtheater.user

import akka.stream.Materializer
import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.auth.{Ctx, UserCtx}
import dev.akif.dreamtheater.common.base.Controller
import dev.akif.dreamtheater.common.{PublicActions, UserActions}
import dev.akif.dreamtheater.session.SessionService
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import zio.Runtime

import scala.concurrent.ExecutionContext

class UserController(override val userService: UserService,
                     override val sessionService: SessionService,
                     cc: ControllerComponents)(override implicit val runtime: Runtime[_],
                                               override implicit val m: Materializer,
                                               override implicit val ec: ExecutionContext) extends Controller(cc)
                                                                                              with PublicActions
                                                                                              with UserActions {
  val register: Action[AnyContent] =
    publicActionParsing[RegisterUserRequest] { ctx: Ctx[RegisterUserRequest] =>
      userService.register(ctx.in).map {
        case (user, session) =>
          succeed(ctx, user).withHeaders(UserCtx.sessionTokenHeaderName -> session.token)
      }
    }

  val login: Action[AnyContent] =
    publicActionParsing[LoginUserRequest] { ctx: Ctx[LoginUserRequest] =>
      userService.login(ctx.in).map {
        case (user, session) =>
          succeed(ctx, user).withHeaders(UserCtx.sessionTokenHeaderName -> session.token)
      }
    }

  val me: Action[AnyContent] =
    userActionReturning[User] { ctx: UserCtx[AnyContent] =>
      Z.succeed(ctx.user)
    }
}
