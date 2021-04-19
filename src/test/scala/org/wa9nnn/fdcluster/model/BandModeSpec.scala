package org.wa9nnn.fdcluster.model

import com.fasterxml.jackson.core.JsonParseException
import org.specs2.mutable.Specification
import play.api.libs.json.Json


class BandModeSpec extends Specification{
  "BandMode" >> {
    "default" >> {
      val bandMode = BandMode()
      bandMode.bandName must beEqualTo ( "20m")
      bandMode.modeName must beEqualTo ( "PH")
      bandMode.toString must beEqualTo ("20m PH")
    }
    "round trip json" >> {
      val bandMode = BandMode("1.25m", "DI")
      val sJson = Json.prettyPrint(Json.toJson(bandMode))
      val backAgain = Json.parse(sJson).as[BandMode]
      backAgain must beEqualTo (bandMode)
    }

    "Bad json" >> {
       Json.parse("crap").as[BandMode] must throwAn[JsonParseException]

    }
  }
}
