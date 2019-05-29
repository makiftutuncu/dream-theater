package com.github.makiftutuncu.dreamtheater.services

import java.util.UUID

import com.github.makiftutuncu.dreamtheater.errors.{APIError, Errors}
import com.github.makiftutuncu.dreamtheater.models.{Session, User}
import com.github.makiftutuncu.dreamtheater.repositories.UserRepository
import com.github.makiftutuncu.dreamtheater.utilities.{PasswordUtils, ZDT}
import com.github.makiftutuncu.dreamtheater.views.{LoginUserRequest, RegisterUserRequest}
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(passwordUtils: PasswordUtils,
                            userRepository: UserRepository,
                            sessionService: SessionService) extends Service with Logging {
  def register(request: RegisterUserRequest)(implicit ec: ExecutionContext): Future[Either[APIError, User]] = {
    val salt = passwordUtils.generateSalt()

    val user = User(
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

    userRepository.insert(user)
  }

  def login(request: LoginUserRequest)(implicit ec: ExecutionContext): Future[Either[APIError, (User, Session)]] = {
    userRepository.getByEmail(request.email).flatMap {
      case Left(getUserByEmailError) =>
        Future.successful(Left(getUserByEmailError))

      case Right(None) =>
        logger.warn(s"User is not found for email '${request.email}'")
        Future.successful(Left(Errors.invalidLogin))

      case Right(Some(user)) =>
        val password = passwordUtils.hash(request.password, user.salt)

        if (password != user.password) {
          logger.warn(s"User password is invalid for email '${request.email}'")
          Future.successful(Left(Errors.invalidLogin))
        } else {
          sessionService.create(user.id).map {
            _.map(s => user -> s)
          }
        }
    }
  }
}
