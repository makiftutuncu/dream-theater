package com.github.makiftutuncu.dreamtheater.services

import com.github.makiftutuncu.dreamtheater.Z
import com.github.makiftutuncu.dreamtheater.errors.E
import com.github.makiftutuncu.dreamtheater.errors.E.Code
import com.github.makiftutuncu.dreamtheater.models.Gender
import javax.inject.Singleton
import play.api.Logging
import zio.ZIO

import scala.util.Try

@Singleton
class GenderService extends Service with Logging {
  def get(key: String): Z[Gender] = ZIO.fromTry(Try(Gender.from(key))).mapError(t => E(Code.unknown, "invalid-key", "Invalid key!", Map("cause" -> t.getMessage)))

  def getAll: Z[List[Gender]] = ZIO.succeed(Gender.all)
}
