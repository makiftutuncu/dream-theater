package com.github.makiftutuncu.dreamtheater.utilities

import com.github.makiftutuncu.dreamtheater.models.User
import play.api.mvc.Request

final case class UserContext[A](override val request: Request[A], user: User) extends Context[A](request)
