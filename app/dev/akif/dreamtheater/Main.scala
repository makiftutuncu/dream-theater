package dev.akif.dreamtheater

import play.api.ApplicationLoader.Context
import play.api.{Application, ApplicationLoader, LoggerConfigurator}

class Main extends ApplicationLoader {
  override def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach { loggerConfigurator =>
      loggerConfigurator.configure(context.environment)
    }

    val components = new Components(context)
    components.start()

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = components.stop()
    })

    components.application
  }
}
