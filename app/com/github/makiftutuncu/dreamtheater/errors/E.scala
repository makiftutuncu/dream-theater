package com.github.makiftutuncu.dreamtheater.errors

import com.github.makiftutuncu.dreamtheater.errors.E.Code
import play.api.libs.json.{Json, Writes}

final case class E(code: Code,
                   name: String,
                   message: String,
                   data: Map[String, String])

object E {
  implicit val eWrites: Writes[E] = Writes[E] { e: E =>
    Json.obj(
      "name" -> e.name,
      "message" -> e.message,
      "data" -> e.data
    )
  }

  final case class Code(value: Int) extends AnyVal

  object Code {
    val unknown = Code(0)
  }
}
