package com.github.makiftutuncu.dreamtheater.models

sealed trait UserType extends EnumItem

object UserType extends Enum[UserType] {
  final case object Mobile    extends UserType { override val index: Int = 0 }
  final case object Dashboard extends UserType { override val index: Int = 1 }

  override val all: List[UserType] = List(Mobile, Dashboard)
}
