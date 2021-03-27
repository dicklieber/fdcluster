/*
 * Copyright (C) 2017  Dick Lieber, WA9NNN
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wa9nnn.fdcluster.model

import com.fasterxml.jackson.core.JsonParseException
import org.wa9nnn.fdcluster.javafx.entry.Sections
import play.api.libs.json.Json
import org.wa9nnn.fdcluster.model.Exchange
class ExchangeSpec extends org.specs2.mutable.Specification {
  "Exchange" >> {
    val exchange = Exchange("10O", "WPA")
    "toString" >> {
      exchange.toString must beEqualTo("10O;WPA")
    }

    "apply" >> {
      val e = Exchange("1H", "AB")
      e.display must beEqualTo ("1O AB")
    }
    "round trip toString" >> {
      val backAgain = Exchange(exchange.toString)
      backAgain must beEqualTo(exchange)
    }
    "round trip toString bad" >> {
      Exchange("crap") must throwAn[IllegalArgumentException]
    }
    "round trip json" >> {
      val json = Json.prettyPrint(Json.toJson(exchange))
      json must beEqualTo(""""10O;WPA"""")
      val backAgain = Json.parse(json).as[Exchange]
      backAgain must beEqualTo(exchange)
    }
    "round trip default" >> {
      val d = new Exchange()
      val json = Json.prettyPrint(Json.toJson(d))
      json must beEqualTo(""""1O;AB"""")
      val backAgain = Json.parse(json).as[Exchange]
      backAgain must beEqualTo(d)
    }
    "parse json bad" >> {
      Json.parse("crap").as[Exchange] must throwAn[JsonParseException]
    }

    "hashcode" >> {
      val hash = exchange.hashCode()
      hash must beEqualTo (1594488)//not much of a test
    }

    "mnomonics" >> {
      val d = new Exchange()
      d.mnomonics must beEqualTo ("1 Oscar Alpha Bravo")
      d.nTtransmitters must beEqualTo (1)
      d.sectionCode must beEqualTo ("AB")
      d.category must beEqualTo ("O")
    }

    "defaultApply" >> {
      val e = new Exchange()
      e.transmitters must beEqualTo (1)
      e.sectionCode must beEqualTo ("AB")
      e.toString must beEqualTo ("1O;AB")
      e.display must beEqualTo ("1O AB")
    }
  }

}
