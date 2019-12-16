package dev.akif.dreamtheater.auth

import dev.akif.dreamtheater.session.Session
import dev.akif.dreamtheater.user.User
import play.api.mvc.{AnyContent, Request}

class UserCtx[+A](override val request: Request[AnyContent],
                  override val in: A,
                  override val requestId: String,
                  val user: User,
                  val userSession: Session) extends Ctx[A](request, in, requestId)

object UserCtx {
  val sessionTokenHeaderName: String = "X-Session-Token"
}
