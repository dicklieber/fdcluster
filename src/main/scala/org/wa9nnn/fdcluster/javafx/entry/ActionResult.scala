
package org.wa9nnn.fdcluster.javafx.entry

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import org.wa9nnn.fdcluster.model.BandMode
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import org.wa9nnn.fdcluster.store.{Search, SearchResult}
import org.wa9nnn.util.{StructuredLogging, WithDisposition}
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, VBox}
import scalafx.scene.text.Text

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class ActionResult(storeActor: ActorRef)(implicit timeout: Timeout, bandMode: ObjectProperty[BandMode]) extends StructuredLogging {

  def potentiaDup(partial: CallSign): Unit = {
    val future: Future[Any] = storeActor ? Search(partial, bandMode.value)
    future.onComplete {
      case Failure(exception) =>
        logger.error(s"Search for dup: $partial", exception)
      case Success(searchResult:SearchResult) =>
        Platform.runLater {
          clear()
          searchResult.qsos.foreach { qsoRecord =>
            val qsoBandMode = qsoRecord.qso.bandMode.bandMode
            add(new Label(s"${qsoRecord.qso.callsign}") {
              styleClass.addAll ( "qsoField", "sadQso", "sad")
//              fill = Color.Blue
            })
          }
          add(new Label(searchResult.display()))
          done()
        }
    }
  }

  def children(value: Seq[Label]): Unit = {
    vbox.children = value
  }

  def update(text: Text): Unit = {
    vbox.children = Seq(text)
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
