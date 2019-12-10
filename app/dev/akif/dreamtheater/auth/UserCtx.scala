package dev.akif.dreamtheater.auth

import dev.akif.dreamtheater.session.Session
import dev.akif.dreamtheater.user.User
import play.api.mvc.Request

class UserCtx[A](override val request: Request[A], val user: User, val userSession: Session) extends Ctx[A](request)

object UserCtx {
  val sessionTokenHeaderName: String = "X-Session-Token"
}
