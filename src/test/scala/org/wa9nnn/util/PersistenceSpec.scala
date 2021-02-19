package org.wa9nnn.util

import org.apache.commons.io.FileUtils._
import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import org.wa9nnn.fdcluster.model.BandMode
import org.wa9nnn.fdcluster.model.MessageFormats._

import java.nio.file.{Files, Path, Paths}


trait PreferencesContext extends ForEach[Persistence] {
  def foreach[R: AsResult](r: Persistence => R): Result = {
    val path: Path = Files.createTempDirectory("PersistenceSpec")
    val persistence = new Persistence(path.toString)
    try AsResult(r(persistence))
    finally deleteDirectory(path.toFile)
  }
}

class PersistenceSpec extends Specification with PreferencesContext {
  "Persistence" >> {
    "save" >> { persistence: Persistence =>
      val exchange = new BandMode("20M", "DI")
      val triedString = persistence.saveToFile(exchange, pretty = false)
      triedString must beASuccessfulTry[String].which(_.endsWith("BandMode.json"))
    }
   "save pretty" >> { persistence: Persistence =>
      val exchange = new BandMode("20M", "DI")
      val triedString = persistence.saveToFile(exchange)
      triedString must beASuccessfulTry[String].which(_.endsWith("BandMode.json"))
     val path = Paths.get(triedString.get)
     Files.readString(path) must beEqualTo ("""{
                                              |  "bandName" : "20M",
                                              |  "modeName" : "DI"
                                              |}""".stripMargin)
    }

    "roundTrip" >> { persistence: Persistence =>
      val exchange = new BandMode("20M", "DI")
      persistence.saveToFile(exchange, pretty = false)
      val backAgain = persistence.loadFromFile[BandMode]()
      backAgain must beSuccessfulTry(exchange)
    }

    "nofile" >> { persistence: Persistence =>
      val instance = persistence.loadFromFile[BandMode]()
      instance must beFailedTry[BandMode]
    }

    "not case class" >> { persistence: Persistence =>
      val instance = persistence.saveToFile[String]("dd")
      instance must beFailedTry[String].withThrowable[IllegalArgumentException]
    }


  }
}

case class NoFormat(eny: Int, miny: String)
