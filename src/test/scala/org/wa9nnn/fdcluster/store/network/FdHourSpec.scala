package org.wa9nnn.fdcluster.store.network

import java.time.LocalDate

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.store.network.FdHour

class FdHourSpec extends Specification {

  "FdHourSpec" >> {
    "plus" >> {
      "same day" >> {
        val date =  LocalDate.of(2016, 1, 10)
        val fdHour = FdHour(date, 5)
        val next = fdHour.plus(1)
        next.localDate mustEqual date
        ok
      }
      "wrap day" >> {
        val date =  LocalDate.of(2016, 1, 10)
        val fdHour = FdHour(date, 23)
        val next = fdHour.plus(1)
        next.localDate mustEqual date.plusDays(1)
        next.hour mustEqual 0
        ok
      }
    }
    "bad hour" >> {
      FdHour(LocalDate.now(), 24) must throwAn[AssertionError]
    }

  }
}
