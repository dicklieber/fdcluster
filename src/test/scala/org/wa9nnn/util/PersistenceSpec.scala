package org.wa9nnn.util

import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import org.wa9nnn.fdcluster.MockFileContext
import org.wa9nnn.fdcluster.model.Station
import org.wa9nnn.fdcluster.model.MessageFormats._


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
    "roundTrip" >> { persistence: Persistence =>
      val currentStation = new Station(
        "20M",
        "DI",
        rig = "IC-705",
        operator = "WA9NNN",
        antenna = "Wold River Coils")
      persistence.saveToFile(currentStation)
      val backAgain: Station = persistence.loadFromFile[Station](() => Station())
      backAgain must beEqualTo(currentStation)
    }

    "nofile" >> { persistence: Persistence =>
      val instance = persistence.loadFromFile[Station](() => Station())
      instance must beAnInstanceOf[Station]
    }
  }
}

case class NoFormat(eny: Int, miny: String)
