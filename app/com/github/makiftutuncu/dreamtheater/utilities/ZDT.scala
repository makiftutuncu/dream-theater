package com.github.makiftutuncu.dreamtheater.utilities

import java.time.ZonedDateTime

object ZDT {
  def now: ZonedDateTime = ZonedDateTime.now.withNano(0)
}
