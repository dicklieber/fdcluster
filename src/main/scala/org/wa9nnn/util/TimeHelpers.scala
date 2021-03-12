
package org.wa9nnn.util

import java.time.format.DateTimeFormatter
import java.time.{Duration, Instant, ZoneId, ZonedDateTime}

import scala.language.implicitConversions

object TimeHelpers {

  def nano2Second(nanoseconds: Double): Double = nanoseconds / 1000000000.0

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss z")

  @scala.inline
  implicit def stringToInstant(in: String): Instant = {
    Instant.parse(in)
  }

  @scala.inline
  implicit def instantToString(instant: Instant): String = {
    formatter.format(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()))
  }

  @scala.inline
  implicit def durationToString(duration: Duration): String = {
    DurationFormat(duration)
  }
  def localFrom(instant: Instant):String = {
    val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
    zonedDateTime.format(formatter)
  }
  
  def parseInstant(in: String): Instant = Instant.parse(in)

  val msHour: Int = 1000 * 60 * 60
  val utcZoneId: ZoneId = ZoneId.of("UTC")

}
