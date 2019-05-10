package com.github.makiftutuncu.dreamtheater.models

sealed trait Gender extends Enum

object Gender {
  final case object Female extends Gender { override val index: Int = 0 }
  final case object Male   extends Gender { override val index: Int = 1 }
}
