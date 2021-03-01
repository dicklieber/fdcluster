package org.wa9nnn.fdcluster.tools

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfo, RunningTaskInfoConsumer}
import org.wa9nnn.fdcluster.javafx.menu.BuildLoadRequest
import org.wa9nnn.util.LaurelDbImporterTask

import java.nio.file.Paths

class LaurelDbImporterSpec extends Specification {

  "LaurelDbImporterSpec" should {
    "apply" in {
      val path = Paths.get("/Users/dlieber/dev/ham/fdcluster/src/test/resource/HD.csv").toString
      val blr = BuildLoadRequest(path, 10)

      val rtc = new RunningTaskInfoConsumer {
        override def update(info: RunningTaskInfo): Unit = {

        }

        override def done(): Unit = {

        }
      }
//      LaurelDbImporterTask(blr, rtc)({ (qso) =>
//        println(qso)
//        true
//      })
      failure
    }
  }
}
