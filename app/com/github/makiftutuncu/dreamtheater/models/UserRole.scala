package com.github.makiftutuncu.dreamtheater.models

sealed trait UserRole extends Enum {
  val rank: Int = index
}

object UserRole {
  final case object Anonymous extends UserRole { override val index: Int = 0 }
  final case object Regular   extends UserRole { override val index: Int = 1 }
  final case object Admin     extends UserRole { override val index: Int = 2 }
}
