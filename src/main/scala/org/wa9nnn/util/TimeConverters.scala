
package com.here.traffic.b2b.util

import java.time.format.DateTimeFormatter
import java.time.{Duration, Instant, LocalDateTime, ZoneId, ZonedDateTime}

import org.wa9nnn.util.DurationFormat

import scala.language.implicitConversions

object TimeConverters {

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


//  def localAndUtc(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String = {
//    val local = formatter.format(ZonedDateTime.ofInstant(instant, zoneId))
//    s"$local (${instant.toString}})"
//  }
//  def localAndUtc(ldt: LocalDateTime, zoneId: ZoneId = ZoneId.systemDefault()): String = {
//    val local = formatter.format(ldt)
//    s"$local (${ldt.toString}})"
//  }

  def parseInstant(in: String): Instant = Instant.parse(in)
}
