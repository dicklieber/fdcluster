package org.wa9nnn.util

import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import org.wa9nnn.fdcluster.MockFileManager
import org.wa9nnn.fdcluster.model.CurrentStation
import org.wa9nnn.fdcluster.model.MessageFormats._

import java.nio.file.{Files, Paths}


trait PreferencesContext extends ForEach[Persistence] {
  def foreach[R: AsResult](r: Persistence => R): Result = {
   val fileManager =  MockFileManager()
    val persistence = new PersistenceImpl(fileManager)
    try AsResult(r(persistence))
    finally fileManager.clean()
  }
}

class PersistenceSpec extends Specification with PreferencesContext {
  "Persistence" >> {
    "save" >> { persistence: Persistence =>
      val exchange = new CurrentStation("20M", "DI")
      val triedString = persistence.saveToFile(exchange, pretty = false)
      triedString must beASuccessfulTry[String].which(_.endsWith("BandModeOperator.json"))
    }
   "save pretty" >> { persistence: Persistence =>
      val exchange = new CurrentStation("20M", "DI")
      val triedString = persistence.saveToFile(exchange)
      triedString must beASuccessfulTry[String].which(_.endsWith("BandModeOperator.json"))
     val path = Paths.get(triedString.get)
     Files.readString(path) must beEqualTo ("""{
                                              |  "bandName" : "20M",
                                              |  "modeName" : "DI",
                                              |  "operator" : ""
                                              |}""".stripMargin)
    }

    "roundTrip" >> { persistence: Persistence =>
      val currentStation = new CurrentStation("20M", "DI")
      persistence.saveToFile(currentStation, pretty = false)
      val backAgain: CurrentStation = persistence.loadFromFile[CurrentStation](() => CurrentStation())
      backAgain must be(currentStation)
    }

    "nofile" >> { persistence: Persistence =>
      val instance = persistence.loadFromFile[CurrentStation](() => CurrentStation())
      instance must beAnInstanceOf[CurrentStation]
    }

    "not case class" >> { persistence: Persistence =>
      val instance = persistence.saveToFile[String]("dd")
      instance must beFailedTry[String].withThrowable[IllegalArgumentException]
    }


  }
}

case class NoFormat(eny: Int, miny: String)
