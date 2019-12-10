package dev.akif.dreamtheater.user

import java.util.UUID

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.auth.PasswordUtils
import dev.akif.dreamtheater.common.base.Service
import dev.akif.dreamtheater.common.{Errors, ZDT}
import dev.akif.dreamtheater.session.{Session, SessionService}
import play.api.Logging
import zio.ZIO

class UserService(passwordUtils: PasswordUtils,
                  userRepository: UserRepository,
                  sessionService: SessionService) extends Service with Logging {
  def getById(id: UUID): Z[Option[User]] = userRepository.getById(id)

  def register(request: RegisterUserRequest): Z[(User, Session)] = {
    val salt = passwordUtils.generateSalt()

    val newUser = User(
      id        = UUID.randomUUID,
      email     = request.email,
      password  = passwordUtils.hash(request.password, salt),
      salt      = salt,
      firstName = request.firstName,
      lastName  = request.lastName,
      gender    = request.gender,
      birthDate = request.birthDate,
      createdAt = ZDT.now,
      updatedAt = ZDT.now,
      deletedAt = None
    )

    for {
      user    <- userRepository.insert(newUser)
      session <- sessionService.create(user.id)
    } yield {
      user -> session
    }
  }

  def login(request: LoginUserRequest): Z[(User, Session)] =
    userRepository.getByEmail(request.email).flatMap {
      case None =>
        logger.warn(s"User is not found for email '${request.email}'")
        ZIO.fail(Errors.invalidLogin)

      case Some(user) =>
        val password = passwordUtils.hash(request.password, user.salt)

        if (password != user.password) {
          logger.warn(s"User password is invalid for email '${request.email}'")
          ZIO.fail(Errors.invalidLogin)
        } else {
          sessionService.create(user.id).map { session =>
            user -> session
          }
        }
    }
}
