package org.wa9nnn.fdcluster.store

import java.time.{Duration, LocalDateTime, ZonedDateTime}
import org.specs2.mutable.Specification
import org.wa9nnn.util.TimeHelpers

class QsoGeneratorSpec extends Specification {

  "QsoGeneratorSpec" should {
    "apply" in {

      val numberOfQsos = 50000
      val qsoRecords = QsoGenerator(
        numberOfQsos, Duration.ofMinutes(1),
        ZonedDateTime.of(2019, 6, 23, 12, 0, 0, 0,TimeHelpers.utcZoneId).toInstant)
      qsoRecords.foreach { qr ⇒
        println(qr.toString)
        //        println(Json.prettyPrint(Json.toJson(qr)))
      }

      qsoRecords must have size numberOfQsos
    }

  }
}
