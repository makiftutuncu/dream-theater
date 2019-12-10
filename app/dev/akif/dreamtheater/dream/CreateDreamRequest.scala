package dev.akif.dreamtheater.dream

import play.api.libs.json.{Json, Reads, Writes}

final case class CreateDreamRequest(title: String,
                                    body: String,
                                    attachmentURL: Option[String]) {
  override def toString: String = Json.toJson(this).toString
}

object CreateDreamRequest {
  implicit val createDreamRequestReads: Reads[CreateDreamRequest] = Json.reads[CreateDreamRequest]

  implicit val createDreamRequestWrites: Writes[CreateDreamRequest] = Json.writes[CreateDreamRequest]
}
