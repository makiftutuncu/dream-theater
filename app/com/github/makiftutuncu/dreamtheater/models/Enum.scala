package com.github.makiftutuncu.dreamtheater.models

import play.api.libs.json._

import scala.language.implicitConversions

trait EnumItem {
  val index: Int
}

trait Enum[I <: EnumItem] { self =>
  val all: List[I]

  implicit val enumReads: Reads[I] =
    Reads[I] {
      case JsNumber(i) => JsSuccess(from(i.intValue()))
      case json        => JsError(s"'$json' is not a valid ${self.getClass.getSimpleName} index value!")
    }

  implicit val enumWrites: Writes[I] = Writes[I](i => JsNumber(i.index))

  def from(index: Int): I = map.getOrElse(index, throw new IllegalArgumentException(s"'$index' is not a valid ${self.getClass.getSimpleName} index value!"))

  lazy val set: Set[I] = all.toSet

  private lazy val map: Map[Int, I] = all.foldLeft(Map.empty[Int, I]) { case (m, i) => m + (i.index -> i) }
}
