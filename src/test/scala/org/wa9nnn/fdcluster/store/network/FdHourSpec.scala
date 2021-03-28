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

    val fdHour = FdHour(instant)
    "from epoch hours" >> {
      val fdHour1 = FdHour(403489)
      fdHour1.epochHours must beEqualTo(403489)
      fdHour1.toString must beEqualTo("12:1")
      fdHour1.display must beEqualTo("12:01")
    }
    "happy path" >> {
      fdHour.epochHours must beEqualTo(403489)
      val string = fdHour.toString
      string must beEqualTo("12:1")
      fdHour.toolTip must beEqualTo("utc date: 12 hour: 1")
    }
    "zero hour in epoch" >> {
      val fdh = FdHour(Instant.EPOCH)
      fdh.epochHours must beEqualTo(0)
    }
    "hour one in epoch" >> {
      val fdh = FdHour(Instant.EPOCH.plusMillis(msHour + 20))
      fdh.epochHours must beEqualTo(1)
    }
    "same instances of singleton" >> {
      val fdh1 = FdHour(Instant.EPOCH.plusMillis(msHour + 20))
      val b4 = FdHour.knownHours
      val fdh2 = FdHour(Instant.EPOCH.plusMillis(msHour + 20))
      val after = FdHour.knownHours

      b4 must beEqualTo(after)
      fdh1 must beTheSameAs(fdh2)
    }
    "ordered" >> {
      val fdh4 = FdHour(4)
      val fdh2 = FdHour(200)
      val fdh1 = FdHour(1)
      val unordered = Seq(fdh4, fdh2, fdh1)
      val ordered = unordered.sorted
      unordered must not be equalTo(ordered)
      ordered.head must beEqualTo(fdh1)
      ordered.last must beEqualTo(fdh2)
    }

    "plus" >> {
      val fdh1 = FdHour(1)
      val fdh2 = fdh1.plus(10)
      fdh2.epochHours must beEqualTo(11)
    }
  }
  "known" >> {
    FdHour.knownHours must haveSize(3)
  }

}
