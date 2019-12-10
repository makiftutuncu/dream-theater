package dev.akif.dreamtheater.user

import dev.akif.dreamtheater.common.base.{Enum, EnumItem}

sealed abstract class Gender(override val key: String) extends EnumItem

object Gender extends Enum[Gender] {
  final case object Female extends Gender("female")
  final case object Male   extends Gender("male")

  override val all: List[Gender] = List(Female, Male)
}
