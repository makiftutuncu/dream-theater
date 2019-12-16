package dev.akif.dreamtheater.common

import java.time.ZonedDateTime

object ZDT {
  def now: ZonedDateTime = ZonedDateTime.now.withNano(0).withFixedOffsetZone()
}
