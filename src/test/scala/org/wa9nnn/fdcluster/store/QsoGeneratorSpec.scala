package org.wa9nnn.fdcluster.store

import java.time.{Duration, LocalDateTime}

import org.specs2.mutable.Specification

class QsoGeneratorSpec extends Specification {

  "QsoGeneratorSpec" should {
    "apply" in {

      val numberOfQsos = 50000
      val qsoRecords = QsoGenerator(
        numberOfQsos, Duration.ofMinutes(1),
        LocalDateTime.of(2019, 6, 23, 12, 0, 0))
      qsoRecords.foreach { qr ⇒
        println(qr.toString)
        //        println(Json.prettyPrint(Json.toJson(qr)))
      }

      qsoRecords must have size numberOfQsos
    }

  }
}
