package org.wa9nnn.fdcluster.contest.fieldday

import com.typesafe.config.ConfigFactory
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.QsoSource
import scalafx.beans.property.StringProperty

import java.awt.Desktop
import java.io.StringWriter
import java.nio.file.Files

class SummaryEngineSpec extends Specification with Mockito {
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
      val contestProperty = mock[ContestProperty]
      val eventProperty: StringProperty = new StringProperty("FieldDay")
      contestProperty.contestNameProperty returns eventProperty
      val allContestRules = mock[AllContestRules]
      val allQsos: Seq[QsoRecord] = Seq(
        QsoRecord(Qso("KD9BYW", Exchange(), BandMode()), QsoMetadata()),
        QsoRecord(Qso("KD9BYW", Exchange(),  BandMode("160m")), QsoMetadata()),
        QsoRecord(Qso("KD9BYW",  Exchange(), BandMode(modeName = "DI")), QsoMetadata()),
        QsoRecord(Qso("NE9A", Exchange(), BandMode(modeName = "DI")), QsoMetadata()),
        QsoRecord(Qso("W9BBQ", Exchange(), BandMode(modeName = "DI")), QsoMetadata()),
      )

      val qsoSource = mock[QsoSource]
      qsoSource.qsoIterator returns(allQsos)

      val summaryEngine = new SummaryEngine(allContestRules, new BandModeBreakDown(qsoSource, allContestRules:AllContestRules))
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
