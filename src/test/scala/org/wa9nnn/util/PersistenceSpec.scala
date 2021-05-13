package org.wa9nnn.util

import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import org.wa9nnn.fdcluster.MockFileContext
import org.wa9nnn.fdcluster.model.CurrentStation
import org.wa9nnn.fdcluster.model.MessageFormats._

import java.nio.file.{Files, Paths}


trait PreferencesContext extends ForEach[Persistence] {
  def foreach[R: AsResult](r: Persistence => R): Result = {
    val fileManager = MockFileContext()
    val persistence = new PersistenceImpl(fileManager)
    try AsResult(r(persistence))
    finally fileManager.clean()
  }
}

class PersistenceSpec extends Specification with PreferencesContext {
  "Persistence" >> {
    "save" >> { persistence: Persistence =>
      val exchange = new CurrentStation("20M", "DI")
      val triedString = persistence.saveToFile(exchange)
      triedString must beASuccessfulTry[String].which(_.endsWith("CurrentStation.json"))
    }
    "save pretty" >> { persistence: Persistence =>
      val exchange = new CurrentStation("20M", "DI")
      val triedString = persistence.saveToFile(exchange)
      triedString must beASuccessfulTry[String].which(_.endsWith("CurrentStation.json"))
      val path = Paths.get(triedString.get)
      Files.readString(path) must beEqualTo(
        """{
          |  "bandName" : "20M",
          |  "modeName" : "DI",
          |  "operator" : "",
          |  "rig" : "",
          |  "antenna" : ""
          |}""".stripMargin)
    }

    "roundTrip" >> { persistence: Persistence =>
      val currentStation = new CurrentStation(
        "20M",
        "DI",
        rig = "IC-705",
        operator = "WA9NNN",
        antenna = "Wold River Coils")
      persistence.saveToFile(currentStation)
      val backAgain: CurrentStation = persistence.loadFromFile[CurrentStation](() => CurrentStation())
      backAgain must beEqualTo(currentStation)
    }

    "nofile" >> { persistence: Persistence =>
      val instance = persistence.loadFromFile[CurrentStation](() => CurrentStation())
      instance must beAnInstanceOf[CurrentStation]
    }

    "not case class" >> { persistence: Persistence =>
      persistence.saveToFile[String]("dd") must throwAn[AssertionError]
    }


  }
}

case class NoFormat(eny: Int, miny: String)
