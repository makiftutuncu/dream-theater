package com.github.makiftutuncu

import com.github.makiftutuncu.dreamtheater.errors.E
import zio.ZIO

package object dreamtheater {
  type Z[+A]        = ZIO[Any, E, A]
  type ZZ[-ENV, +A] = ZIO[ENV, E, A]
}
