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

import java.net.InetAddress
import java.nio.file.{Files, Path}

import org.specs2.mutable.Specification
import org.specs2.specification.After
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model._

import scala.collection.mutable

class StoreMapImplSpec extends Specification with After{
  val expectedNodeAddress: String = InetAddress.getLocalHost.getHostAddress

  implicit val nodeInfo: NodeInfoImpl = new NodeInfoImpl(
    contest = Contest("WFD", 2017),
    nodeAddress = expectedNodeAddress)


  private val journal: Path = Files.createTempFile("fdlog-journal", ".log")
  private val storeMapImpl = new StoreMapImpl(nodeInfo, new CurrentStationProviderImpl(), Some(journal))
  private val worked: CallSign = "K2ORS"
  private val exchange: Exchange = Exchange("2I", "WPA")
  private val bandMode = BandMode(Band("20m"), Mode.phone)

  "StoreMapImplSpec" >> {
    "happy path" >> {
      val maybeAddedContact: AddResult = storeMapImpl.add(Qso(worked, bandMode, exchange))
      maybeAddedContact must beAnInstanceOf[Added]

      val contactIds = storeMapImpl.contactIds
      contactIds.contactIds must haveSize(1)
      contactIds.node must beEqualTo(expectedNodeAddress)
    }
  }
  after

  override def after: Any = {
    Files.delete(journal)
  }
}
