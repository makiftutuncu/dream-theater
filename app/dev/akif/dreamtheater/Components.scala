package dev.akif.dreamtheater

import dev.akif.dreamtheater.auth.PasswordUtils
import dev.akif.dreamtheater.dream.{DreamController, DreamRepository, DreamService}
import dev.akif.dreamtheater.session.{SessionRepository, SessionService}
import dev.akif.dreamtheater.user.{UserController, UserRepository, UserService}
import play.api.ApplicationLoader.Context
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.http.HttpErrorHandler
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{BuiltInComponentsFromContext, Logging}
import play.filters.HttpFiltersComponents
import play.filters.cors.{CORSConfig, CORSFilter}
import router.Routes
import zio.Runtime
import zio.internal.PlatformLive

class Components(ctx: Context) extends BuiltInComponentsFromContext(ctx)
                                  with DBComponents
                                  with EvolutionsComponents
                                  with HikariCPComponents
                                  with HttpFiltersComponents
                                  with Logging {
  implicit val runtime: Runtime[Any] = Runtime[Any](null, PlatformLive.Default)

  override lazy val httpErrorHandler: HttpErrorHandler = new ErrorHandler

  override def httpFilters: Seq[EssentialFilter] = Seq(new CORSFilter(CORSConfig.fromConfiguration(configuration), httpErrorHandler))

  lazy val database: Database = dbApi.database("default")

  lazy val passwordUtils: PasswordUtils = new PasswordUtils(configuration)

  lazy val sessionRepository: SessionRepository = new SessionRepository
  lazy val userRepository: UserRepository       = new UserRepository
  lazy val dreamRepository: DreamRepository     = new DreamRepository

  lazy val sessionService: SessionService = new SessionService(sessionRepository, database)
  lazy val userService: UserService       = new UserService(passwordUtils, userRepository, sessionRepository, sessionService, database)
  lazy val dreamService: DreamService     = new DreamService(dreamRepository, database)

  lazy val rootController: RootController   = new RootController(controllerComponents)
  lazy val userController: UserController   = new UserController(userService, sessionService, controllerComponents)
  lazy val dreamController: DreamController = new DreamController(dreamService, userService, sessionService, controllerComponents)

  override def router: Router =
    new Routes(
      httpErrorHandler,
      rootController,
      userController,
      dreamController
    )

  def start(): Unit = {
    applicationEvolutions
    logger.info("Dream Theater is up!")
  }

  def stop(): Unit = {
    logger.info("Stoppin Dream Theater...")
  }
}
