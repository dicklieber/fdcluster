package org.wa9nnn.fdcluster.contest

import org.specs2.mutable.Specification
import org.wa9nnn.util.TimeHelpers.utcZoneId

import java.time.{DayOfWeek, Duration, LocalDate, LocalTime, Month, ZonedDateTime}

class FieldDayScheduleSpec extends Specification {

  "FieldDaySchedule" >> {
    "Field Day" >> {
      "date 2021" >> {
        val duration = Duration.ofHours(27)
        val fieldDaySchedule = FieldDaySchedule(2021, ScheduleAlgorithm.fourth, Month.JUNE, LocalTime.of(18, 0), duration)
        val start = fieldDaySchedule.start
        val zonedDateTime = ZonedDateTime.ofInstant(start, utcZoneId)

        zonedDateTime.getYear must beEqualTo(2021)
        zonedDateTime.getMonth must beEqualTo(Month.JUNE)
        zonedDateTime.getDayOfMonth must beEqualTo(26)
        zonedDateTime.getDayOfWeek must beEqualTo (DayOfWeek.SATURDAY)

        fieldDaySchedule.duration must beEqualTo (duration)

      }
    }
    "Winter Field Day" >> {
      pending
    }
  }
}
