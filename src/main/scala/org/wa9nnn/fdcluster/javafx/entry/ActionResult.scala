
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.javafx.entry

import _root_.scalafx.application.Platform
import _root_.scalafx.scene.control.Label
import _root_.scalafx.scene.layout.{Pane, VBox}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import org.wa9nnn.fdcluster.model.{BandMode, QsoMetadata}
import org.wa9nnn.fdcluster.store.{Search, SearchResult}
import org.wa9nnn.util.WithDisposition

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class ActionResult(storeActor: ActorRef, qsoMetadata: QsoMetadata)(implicit timeout: Timeout) extends LazyLogging {

  def potentiaDup(partial: CallSign): Unit = {
    val bm=BandMode()//todo when we re do station panel in EntryScene
    val future: Future[SearchResult] = (storeActor ? Search(partial, bm)).mapTo[SearchResult]
    future.onComplete {
      case Failure(exception) =>
        logger.error(s"Search for dup: $partial", exception)
      case Success(searchResult: SearchResult) =>
        Platform.runLater {
          clear()
          searchResult.qsos.foreach { qsoRecord =>
//            val qsoBandMode = qsoRecord.qso.bandMode
            add(new Label(s"${qsoRecord.qso.callSign}") {
              styleClass.addAll("qsoField", "sadQso", "sad")
            })
          }
          add(new Label(searchResult.display()))
          onFX {
            done()
          }
        }
    }
  }

  def children(value: Seq[Label]): Unit = {
    vbox.children = value
  }

  def showSad(text: String): Unit = {
    vbox.children = Seq(new Label(text))
  }

  private val accum = Seq.newBuilder[Label]
  private val vbox = new VBox()

  def add(text: Label): Unit = {
    accum += text
  }

  def addHappy(str: String): Unit = {
    accum += new Label(str) with WithDisposition {
      happy()
    }
  }

  def addSad(str: String): Unit = {
    accum += new Label(str) with WithDisposition {
      sad()
    }
  }


  def done(): Unit = {
    val value = accum.result()
    vbox.children = value
  }

  def clear(): Unit = accum.clear()

  val pane: Pane = vbox
}
