package com.github.makiftutuncu.dreamtheater.utilities

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import javax.inject.{Inject, Singleton}
import play.api.Configuration

import scala.util.Random

@Singleton
class PasswordUtils @Inject()(config: Configuration) {
  val pepper: String = config.get[String]("dreamTheater.pepper")

  def generateSalt(): String = {
    val chars = for (_ <- 1 to 32) yield "%02x".format(Random.nextPrintableChar.toByte)
    chars.mkString
  }

  def hash(plainPassword: String, salt: String): String =
    MessageDigest
      .getInstance("SHA-256")
      .digest(s"$plainPassword$salt$pepper".getBytes(StandardCharsets.UTF_8))
      .map(b => "%02x".format(b))
      .mkString
}
