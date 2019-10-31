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
import org.wa9nnn.fdcluster.model.Exchange
import play.api.libs.json.Json

class ExchangeSpec extends org.specs2.mutable.Specification {
  "Exchange" >> {
    val exchange = Exchange("10O", "WPA")
    "toString" >> {
      exchange.toString must beEqualTo("10O;WPA")
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
    "parse json bad" >> {
      Json.parse("crap").as[Exchange] must throwAn[JsonParseException]
    }

  }

}
