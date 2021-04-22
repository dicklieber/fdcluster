package org.wa9nnn.fdcluster.contest


import com.github.andyglow.config._
import com.typesafe.config.Config
import org.wa9nnn.util.TimeHelpers.utcZoneId

import java.time.temporal.TemporalAdjusters
import java.time.{LocalDate, Month, _}
import scala.annotation.tailrec


  case class FieldDaySchedule(start: Instant, duration: Duration) {
    val end: Instant = start.plus(duration)

    def inSchedule(candidate: Instant): Boolean = {
      candidate.isAfter(start) && candidate.isBefore(end)
    }
  }

  object FieldDaySchedule {
    /**
     *
     * @param year of interest.
     * @param eventConfig speciically for this event.
     * @return
     */
    def apply( eventConfig: Config, year: Int = ZonedDateTime.now().getYear): FieldDaySchedule = {
      val config = eventConfig.getConfig("schedule")

      val scheduleAlgorithm = ScheduleAlgorithm.valueOf(config.getString("algorithm"))

      val month = Month.valueOf(config.getString("month").toUpperCase)
      val startTimeUtc = config.get[LocalTime]("startTime")
      val duration = config.getDuration("duration")
      apply(year, scheduleAlgorithm, month, startTimeUtc, duration)
    }

    def apply(year: Int, algorithm: ScheduleAlgorithm, month: Month, startTimeUtc: LocalTime, duration: Duration): FieldDaySchedule = {
      val date: LocalDate = algorithm match {
        case ScheduleAlgorithm.last => lastWeekEnd(year, month)
        case ScheduleAlgorithm.fourth => fourthWeekEnd(year, month)
      }

      val zdt: ZonedDateTime = ZonedDateTime.of(date, startTimeUtc, utcZoneId)
      new FieldDaySchedule(zdt.toInstant, duration)
    }

    /**
     * As used for ARRL field day.
     *
     * @param year  of interest.
     * @param month when it occurs
     * @return the date.
     */
    def fourthWeekEnd(year: Int, month: Month): LocalDate = {
      val date: LocalDate = LocalDate.of(year, month, 15) // day not important
      val forthSunday = date.`with`(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.SUNDAY))
      forthSunday.minusDays(1)
    }

    /**
     * As used for Winter Field Day
     *
     * @param year  of interest.
     * @param month when it occurs
     * @return the date.
     */
    def lastWeekEnd(year: Int, month: Month): LocalDate = {
      @tailrec
      def walkBackToSunday(localDate: LocalDate): LocalDate = {
        if (localDate.getDayOfWeek != DayOfWeek.SUNDAY)
          walkBackToSunday(localDate.minusDays(1))
        else
          localDate
      }

      val date: LocalDate = LocalDate.of(year, month, 15) // day not important
      val lastDayOfMonth: LocalDate = date.`with`(TemporalAdjusters.lastDayOfMonth())
      // walk back until a Sunday
      val lastSunday = walkBackToSunday(lastDayOfMonth)
      // walk back one to Saturday
      lastSunday.minusDays(1)

    }
  }



