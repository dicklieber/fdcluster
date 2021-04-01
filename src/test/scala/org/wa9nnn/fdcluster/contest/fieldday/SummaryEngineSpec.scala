package org.wa9nnn.fdcluster.contest.fieldday

import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.{AllContestRules, Exchange}

import java.awt.Desktop
import java.io.StringWriter
import java.nio.file.Files

class SummaryEngineSpec extends Specification {

  "SummaryEngine" should {
    "apply" in {
      val config = ConfigFactory.load()
      val contest: Contest = Contest(callSign = "KD9BYW",
        ourExchange = Exchange("2B", "IL"))

      val wfd = WinterFieldDaySettings(
        gotaCallSign = "KI9DDY",
        club = "WM9W",
        nParticipants = 25
      )

      val allContestRules = new AllContestRules(config)

      val summaryEngine = new SummaryEngine(allContestRules)
      val writer = new StringWriter
      summaryEngine(writer, contest, wfd)
      writer.close()

      val path = Files.createTempFile("SummaryEngineSpec", ".html")
      Files.writeString(path, writer.toString)
      val uri = path.toUri
      Desktop.getDesktop.browse(uri)
      ok
    }
  }
}
