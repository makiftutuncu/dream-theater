package com.github.makiftutuncu.dreamtheater.models

sealed trait UserType extends Enum

object UserType {
  final case object Mobile    extends UserType { override val index: Int = 0 }
  final case object Dashboard extends UserType { override val index: Int = 1 }
}
