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

import com.codahale.metrics.SharedMetricRegistries
import org.apache.commons.io.FileUtils._
import org.specs2.execute.{AsResult, Result}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.{After, ForEach}
import org.wa9nnn.fdcluster.contest.{JournalProperty, JournalWriter}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.network.MulticastSender
import org.wa9nnn.fdcluster.{FileContext, MockFileContext}
import org.wa9nnn.util.{CommandLine, Persistence, PersistenceImpl}
import scalafx.collections.ObservableBuffer

import java.nio.file.{Files, Path}
import java.util.UUID
import scala.util.Try


trait StoreLogicContext extends ForEach[StoreLogic] with Mockito {
  def foreach[R: AsResult](r: StoreLogic => R): Result = {
    val listener = mock[AddQsoListener]
    val journalManager = mock[JournalProperty]
    val journalLoader = mock[JournalLoader]
    val journalWriter = mock[JournalWriter]
    val contestProperty: ContestProperty = mock[ContestProperty]
    val storeSender: StoreSender = mock[StoreSender]
    val multicastSender: MulticastSender = mock[MulticastSender]
    val qsoMetadataProperty: OsoMetadataProperty = mock[OsoMetadataProperty]
    val storeLogic = new StoreLogic(
      na = NodeAddress(),
      qsoMetadataProperty = qsoMetadataProperty,
      contestProperty = contestProperty,
      multicastSender = multicastSender,
      journalManager = journalManager,
      journalLoader = journalLoader,
      journalWriter = journalWriter,
      listeners = Set(listener),
      storeSender = storeSender
    )


    try AsResult(r(storeLogic))
    //    finally fileManager.clean()
  }
}

class StoreLogicSpec extends Specification with After with StoreLogicContext {
  val expectedNodeAddress: NodeAddress = NodeAddress()

  val fileContext: FileContext = MockFileContext()
  private val directory: Path = Files.createTempDirectory("StoreMapImplSpec")
  val allQsos = new ObservableBuffer[Qso]()
  implicit val qsoMetadata: QsoMetadata = QsoMetadata()

  private val worked: CallSign = "K2ORS"
  private val exchange: Exchange = Exchange("2I", "WPA")
  private val bandMode = BandMode()

  "StoreMapImplSpec" >> {
    val qso = Qso("WA9NNN", Exchange(), BandMode())

    "ingest to memory" >> { storeLogic: StoreLogic =>

      val triedQso = storeLogic.ingest(qso)
      triedQso must beSuccessfulTry(qso)
      storeLogic.byUuid must haveSize(1)
      storeLogic.byCallSign must haveSize(1)
      there was one(storeLogic.listeners.head).add(qso)

      val triedQso1: Try[Qso] = storeLogic.ingest(qso)
      triedQso1 must beFailedTry.withThrowable[UuidDup]

      val differentUuid = qso.copy(uuid = UUID.randomUUID())
      val triedQso2: Try[Qso] = storeLogic.ingest(differentUuid)
      triedQso2 must beFailedTry.withThrowable[DupContact]
      there was no(storeLogic.journalWriter).write(qso)
    }
    "ingest and persist" >> { storeLogic: StoreLogic =>
      storeLogic.ingestAndPersist(qso)
      there was one(storeLogic.journalWriter).write(qso)
    }
    "Search" >> { storeLogic: StoreLogic =>
      def q(cs:CallSign, bandMode: BandMode):Unit = {
        storeLogic.ingest(Qso(cs, Exchange(), bandMode ))
      }

      val bm20mCW = BandMode("20m CW")
      q("WA9NNN",  bm20mCW)
      q("WA9DEW",  bm20mCW)
      q("NE9A",  bm20mCW)
      q("W9MOL",  bm20mCW)
      q("K9NNN",  bm20mCW)

      val bm20mDI = BandMode("20m DI")
      q("NE9A",  bm20mDI)

      val result = storeLogic.search(Search("WA9N", bm20mCW))
      result.qsos must haveSize(1)
      result.fullCount must beEqualTo(1)

      val result1 = storeLogic.search(Search("9A", bm20mCW))
      result1.qsos must haveSize(1)
      result1.fullCount must beEqualTo(1)

      val result2 = storeLogic.search(Search("NNN", bm20mCW))
      result2.qsos must haveSize(2)
      result2.fullCount must beEqualTo(2)


    }


  }
  after

  override def after: Any = {
    deleteDirectory(directory.toFile)
  }
}
