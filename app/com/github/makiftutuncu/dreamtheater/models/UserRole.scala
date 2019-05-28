package com.github.makiftutuncu.dreamtheater.models

sealed trait UserRole extends EnumItem {
  val rank: Int = index
}

object UserRole extends Enum[UserRole] {
  final case object Anonymous extends UserRole { override val index: Int = 0 }
  final case object Regular   extends UserRole { override val index: Int = 1 }
  final case object Admin     extends UserRole { override val index: Int = 2 }

  override val all: List[UserRole] = List(Anonymous, Regular, Admin)
}
