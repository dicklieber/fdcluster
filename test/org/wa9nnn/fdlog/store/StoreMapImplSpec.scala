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

package org.wa9nnn.fdlog.store

import org.specs2.mutable.Specification
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.model.Contact._

class StoreMapImplSpec extends Specification {

  implicit val nodeInfo: NodeInfoImpl = new NodeInfoImpl(contest = Contest("WFD", 2017))
  private val storeMapImpl = new StoreMapImpl(nodeInfo)
  private val worked: CallSign = "K2ORS"
  private val exchange: Exchange = Exchange("2I", "WPA")
  implicit val stationContext = StationContext(
    store = storeMapImpl,
    operator = OurStation("WA9NNN", "IC-7300", "Endfed"),
    bandMode = BandMode(Band("20m"), Mode.phone))

  "StoreMapImplSpec" >> {
    "happy path" >> {
      val maybeAddedContact = storeMapImpl.add(Qso(worked, stationContext.bandMode, exchange))
      maybeAddedContact must beSome[QsoRecord]

      val contactIds = storeMapImpl.contactIds
      contactIds.contactIds must haveSize(1)
      contactIds.node must beEqualTo("")
      ok
    }


  }
}
