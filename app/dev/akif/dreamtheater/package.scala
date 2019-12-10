package dev.akif

import dev.akif.e.E
import zio.ZIO

package object dreamtheater {
  type Z[+A] = ZIO[Any, E, A]

  implicit class ZIOOptionExtensions[A](val zio: Z[Option[A]]) {
    def noneZ(e:  => E): Z[A] =
      zio.foldM(
        e => ZIO.fail(e),
        maybe => maybe.fold[Z[A]](ZIO.fail(e))(a => ZIO.succeed(a))
      )
  }
}
