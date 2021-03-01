package org.wa9nnn.fdcluster.tools

import org.mockito.internal.matchers.Any
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfo, RunningTaskInfoConsumer}
import org.wa9nnn.fdcluster.javafx.menu.BuildLoadRequest
import org.wa9nnn.fdcluster.model.{BandModeOperator, Contest, Exchange, FdLogId, OurStation, Qso, QsoRecord}
import org.wa9nnn.fdcluster.store.{AddResult, Added, MockStore, Store}
import org.wa9nnn.util.LaurelDbImporterTask

import java.nio.file.Paths

class LaurelDbImporterSpec extends Specification with Mockito {

  "LaurelDbImporterSpec" should {
    "apply" in {
      val path = Paths.get("/Users/dlieber/dev/ham/fdcluster/src/test/resource/HD.csv").toString
      val buildLoadRequest: BuildLoadRequest = BuildLoadRequest(path, 10)

      val rtc = new RunningTaskInfoConsumer {
        override def update(info: RunningTaskInfo): Unit = {

        }

        override def done(): Unit = {

        }
      }
      val store = new MockStore()
       new LaurelDbImporterTask(store, rtc)(buildLoadRequest)
      store.qsos must haveSize(10)

    }
  }
}
