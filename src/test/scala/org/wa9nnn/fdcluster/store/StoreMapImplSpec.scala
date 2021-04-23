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

package org.wa9nnn.fdcluster.store

import akka.actor.ActorRef
import org.apache.commons.io.FileUtils._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.After
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.{FileManager, MockFileManager}
import org.wa9nnn.util.{CommandLine, PersistenceImpl}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

import java.nio.file.{Files, Path}

class StoreMapImplSpec extends Specification with After with Mockito {
  val expectedNodeAddress: NodeAddress = NodeAddress()

  val fileManager: FileManager = mock[FileManager] //todo need some values
  private val directory: Path = Files.createTempDirectory("StoreMapImplSpec")
  val persistence = new PersistenceImpl(fileManager)
  val allQsos = new ObservableBuffer[QsoRecord]()

  val commandLine: CommandLine = mock[CommandLine].is("skipJournal") returns false

  private val storeMapImpl = new StoreLogic(na = NodeAddress(),
    ObjectProperty(QsoMetadata()),
    allQsos = ObservableBuffer[QsoRecord](Seq.empty),
    fileManager = MockFileManager(),
    multicastSender = mock[ActorRef],
    contestProperty =  new   ContestProperty(persistence, NodeAddress())
  )

  private val worked: CallSign = "K2ORS"
  private val exchange: Exchange = Exchange("2I", "WPA")
  private val bandMode = BandMode()

  "StoreMapImplSpec" >> {
    "happy path" >> {
      val maybeAddedContact: AddResult = storeMapImpl.add(Qso(worked, bandMode, exchange))
      maybeAddedContact must beAnInstanceOf[Added]

      val contactIds = storeMapImpl.contactIds
      contactIds.qsoIds must haveSize(1)
      contactIds.node must beEqualTo(expectedNodeAddress)
    }
  }
  after

  override def after: Any = {
    //    Files.delete(journal)
    deleteDirectory(directory.toFile)
  }
}
