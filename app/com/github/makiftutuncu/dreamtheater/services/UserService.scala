package com.github.makiftutuncu.dreamtheater.services

import java.util.UUID

import com.github.makiftutuncu.dreamtheater.errors.Errors
import com.github.makiftutuncu.dreamtheater.models.{Session, User}
import com.github.makiftutuncu.dreamtheater.repositories.UserRepository
import com.github.makiftutuncu.dreamtheater.utilities.Maybe.FM
import com.github.makiftutuncu.dreamtheater.utilities.{Maybe, PasswordUtils, ZDT}
import com.github.makiftutuncu.dreamtheater.views.{LoginUserRequest, RegisterUserRequest}
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.ExecutionContext

@Singleton
class UserService @Inject()(passwordUtils: PasswordUtils,
                            userRepository: UserRepository,
                            sessionService: SessionService) extends Service with Logging {
  def getById(id: UUID)(implicit ec: ExecutionContext): FM[Option[User]] = userRepository.getById(id)

  def register(request: RegisterUserRequest)(implicit ec: ExecutionContext): FM[(User, Session)] = {
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

    Maybe.flatMapF(userRepository.insert(newUser)) { user =>
      Maybe.mapF(sessionService.create(user.id)) { session =>
        user -> session
      }
    }
  }

  def login(request: LoginUserRequest)(implicit ec: ExecutionContext): FM[(User, Session)] =
    Maybe.flatMapF(userRepository.getByEmail(request.email)) {
      case None =>
        logger.warn(s"User is not found for email '${request.email}'")
        Maybe.errorF(Errors.invalidLogin)

      case Some(user) =>
        val password = passwordUtils.hash(request.password, user.salt)

        if (password != user.password) {
          logger.warn(s"User password is invalid for email '${request.email}'")
          Maybe.errorF(Errors.invalidLogin)
        } else {
          Maybe.mapF(sessionService.create(user.id)) { session =>
            user -> session
          }
        }
    }
}
