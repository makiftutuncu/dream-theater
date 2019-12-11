package dev.akif

import dev.akif.e.E
import zio.ZIO

package object dreamtheater {
  type Z[+A] = ZIO[Any, E, A]

  object Z {
    def fail[A](e: E): Z[A] = ZIO.fail(e)

    def succeed[A](a: => A): Z[A] = ZIO.succeed(a)

    def applyHandlingErrors[A](a: => A)(errorHandler: PartialFunction[Throwable, E] = PartialFunction.empty): Z[A] =
      ZIO.apply(a).mapError { cause =>
        errorHandler.applyOrElse[Throwable, E](cause, t => E.empty.cause(t))
      }

    def apply[A](a: => A): Z[A] = applyHandlingErrors[A](a)(PartialFunction.empty[Throwable, E])
  }

  implicit class ZIOOptionExtensions[A](val zio: Z[Option[A]]) {
    def failIfNone(e: => E): Z[A] =
      zio.foldM(
        e     => Z.fail(e),
        maybe => maybe.fold[Z[A]](Z.fail(e))(a => Z.succeed(a))
      )
  }
}
