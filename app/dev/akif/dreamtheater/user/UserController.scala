package dev.akif.dreamtheater.user

import dev.akif.dreamtheater.auth.{Ctx, UserCtx}
import dev.akif.dreamtheater.common.base.Controller
import dev.akif.dreamtheater.common.{PublicActions, UserActions}
import dev.akif.dreamtheater.session.SessionService
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import zio.{Runtime, ZIO}

class UserController(override val userService: UserService,
                     override val sessionService: SessionService,
                     cc: ControllerComponents)(override implicit val runtime: Runtime[_]) extends Controller(cc)
                                                                                             with PublicActions
                                                                                             with UserActions {
  val register: Action[RegisterUserRequest] =
    publicAction[RegisterUserRequest] { ctx: Ctx[RegisterUserRequest] =>
      userService.register(ctx.body).map {
        case (user, session) =>
          succeed(user).withHeaders(UserCtx.sessionTokenHeaderName -> session.token)
      }
    }

  val login: Action[LoginUserRequest] =
    publicAction[LoginUserRequest] { ctx: Ctx[LoginUserRequest] =>
      userService.login(ctx.body).map {
        case (user, session) =>
          succeed(user).withHeaders(UserCtx.sessionTokenHeaderName -> session.token)
      }
    }

  val me: Action[AnyContent] =
    userAction { ctx: UserCtx[AnyContent] =>
      ZIO.succeed(succeed(ctx.user))
    }
}
