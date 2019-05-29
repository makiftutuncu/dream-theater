package com.github.makiftutuncu.dreamtheater.utilities

import com.github.makiftutuncu.dreamtheater.models.{Session, User}
import play.api.mvc.Request

class UserContext[A](override val request: Request[A], val user: User, val userSession: Session) extends Context[A](request)
