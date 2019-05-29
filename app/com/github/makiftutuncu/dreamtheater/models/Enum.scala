package com.github.makiftutuncu.dreamtheater.models

import play.api.libs.json._

import scala.language.implicitConversions

trait EnumItem {
  val key: String
}

trait Enum[I <: EnumItem] { self =>
  val all: List[I]

  implicit val enumReads: Reads[I] =
    Reads[I] {
      case JsString(s) => JsSuccess(from(s))
      case json        => JsError(s"'$json' is not a valid ${self.getClass.getSimpleName.replaceAll("\\$", "")} value!")
    }

  implicit val enumWrites: Writes[I] = Writes[I](i => JsString(i.key))

  def from(key: String): I = map.getOrElse(key, throw new IllegalArgumentException(s"'$key' is not a valid ${self.getClass.getSimpleName.replaceAll("\\$", "")} value!"))

  lazy val set: Set[I] = all.toSet

  private lazy val map: Map[String, I] = all.foldLeft(Map.empty[String, I]) { case (m, i) => m + (i.key -> i) }
}
