package org.wa9nnn.fdcluster.store.network

import org.specs2.mutable.Specification
import org.wa9nnn.util.TimeHelpers
import org.wa9nnn.util.TimeHelpers.msHour

import java.time.{Instant, LocalDate, LocalTime, ZonedDateTime}

class FdHourSpec extends Specification {

  "FdHourSpec" >> {
    val zdt = ZonedDateTime.of(
      LocalDate.of(2016, 1, 12),
      LocalTime.of(1, 2),
      TimeHelpers.utcZoneId)
    val instant: Instant = zdt.toInstant

    "hour one in epoch" >> {
      val fdh = FdHour(Instant.EPOCH.plusMillis(msHour + 20))
      fdh.day must beEqualTo(1)
    }
    "same instances of singleton" >> {
      val fdh1 = FdHour(Instant.EPOCH.plusMillis(msHour + 20))
      val b4 = FdHour.knownHours
      val fdh2 = FdHour(Instant.EPOCH.plusMillis(msHour + 20))
      val after = FdHour.knownHours

      b4 must beEqualTo(after)
      fdh1 must beTheSameAs(fdh2)
    }

    "plus" >> {
      val fdh1 = FdHour(1,10)
      val fdh2 = fdh1.plus(10)
      fdh2.toString must beEqualTo("1:20")
    }
  }
  "known" >> {
    FdHour.knownHours must haveSize(3)
  }

}
