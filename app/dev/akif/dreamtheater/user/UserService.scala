package dev.akif.dreamtheater.user

import java.util.UUID

import dev.akif.dreamtheater.Z
import dev.akif.dreamtheater.auth.PasswordUtils
import dev.akif.dreamtheater.common.base.Service
import dev.akif.dreamtheater.common.{Errors, ZDT}
import dev.akif.dreamtheater.session.{Session, SessionRepository, SessionService}
import play.api.db.Database

class UserService(passwordUtils: PasswordUtils,
                  userRepository: UserRepository,
                  sessionRepository: SessionRepository,
                  sessionService: SessionService,
                  db: Database) extends Service(db) {
  def getById(id: UUID): Z[Option[User]] =
    withDB { implicit connection =>
      userRepository.getById(id)
    }

  def register(request: RegisterUserRequest): Z[(User, Session)] =
    withDBTransaction { implicit connection =>
      for {
        user    <- userRepository.insert(buildUser(request, passwordUtils.generateSalt()))
        session <- sessionRepository.insert(sessionService.buildSession(user.id))
      } yield {
        user -> session
      }
    }

  def login(request: LoginUserRequest): Z[(User, Session)] =
    withDB { implicit connection =>
      for {
        user     <- userRepository.getByEmail(request.email) failIfNone Errors.invalidLogin
        password  = passwordUtils.hash(request.password, user.salt)
        session  <- if (password != user.password) Z.fail(Errors.invalidLogin) else sessionService.create(user.id)
      } yield {
        user -> session
      }
    }

  def buildUser(request: RegisterUserRequest, salt: String): User =
    User(
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
}
