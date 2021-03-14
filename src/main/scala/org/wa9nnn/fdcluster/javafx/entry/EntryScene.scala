
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

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.javafx.entry.section.SectionField
import org.wa9nnn.fdcluster.javafx.{CallSignField, ClassField, StatusMessage, StatusPane}
import org.wa9nnn.fdcluster.model
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.{AddResult, Added, Dup}
import org.wa9nnn.util.WithDisposition
import play.api.libs.json.Json
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox, VBox}

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Create ScalaFX UI for field day entry mode.
 */
class EntryScene @Inject()(
                            bandModeStore: BandModeOperatorStore,
                            bandModeOpPanel: BandModeOpPanel,
                            ourStationStore: OurStationStore,
                            statsPane: StatsPane,
                            statusPane: StatusPane,
                            @Inject() @Named("store") store: ActorRef) {
  private implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val bandMode = bandModeStore.bandMode

  var actionResult = new ActionResult(store)
  val qsoCallsign = new CallSignField(actionResult)
  val qsoClass = new ClassField()
  private val value: OurStation = ourStationStore.value

  val qsoSection = new SectionField()

  val qsoSubmit = new Button("Log") with WithDisposition
  val clearButton = new Button("Clear") with WithDisposition
  clearButton.onAction = _ => {
    clear()
  }
  qsoSubmit.disable = true
  qsoSubmit.sad()
  val pane: BorderPane = new BorderPane {
    padding = Insets(25)
    private val buttons = new HBox() {
      alignment = Pos.BottomCenter
      spacing = 8
      padding = Insets(10)
      children = List(qsoSubmit, clearButton)
    }
    center = new HBox(
      new VBox(
        new Label("Callsign"),
        qsoCallsign,
        actionResult.pane
      ),
      new VBox(
        new Label("Class"),
        qsoClass,
        new VBox(
          buttons,
          statsPane.pane,
          bandModeOpPanel
        )
      ),
      new VBox(
        new Label("Section"),
        qsoSection,
        qsoSection.sectionPrompt
      )
    )
  }

  val scene: Scene = new Scene {
    root = pane
  }
  qsoCallsign.onDone { next =>
    if (qsoClass.text.value.isEmpty) {
      nextField(next, qsoClass)
    }
  }
  qsoClass.onDone { next =>
    qsoSection.requestFocus()
    qsoSection.clear()
  }
  qsoSection.onDone { _ =>
    qsoSubmit.disable = false
    qsoSubmit.happy()
    save()
  }
  qsoSubmit.onAction = (_: ActionEvent) => {
    save()
  }

  val allFields = new Compositor(qsoCallsign.validProperty, qsoClass.validProperty, qsoSection.validProperty)
  allFields.onChange { (_, _, state) =>
    if (state) {
      qsoSubmit.disable = false
      qsoSubmit.happy()
    } else {
      qsoSubmit.disable = true
      qsoSubmit.sad()
    }
  }

  def save(): Unit = {
    import org.wa9nnn.fdcluster.model.MessageFormats._
    val potentialQso: Qso = readQso()
    if (potentialQso.callsign == ourStationStore.value.ourCallsign) {
      actionResult.showSad(s"Can't work our own station: \n${potentialQso.callsign}!")
    }
    else {
      val future: Future[AddResult] = (store ? potentialQso).mapTo[AddResult]
      future onComplete { tr: Try[AddResult] =>
        actionResult.clear()
        tr match {
          case Failure(exception) =>
          case Success(Dup(dupQso)) =>
            val pretty = Json.prettyPrint(Json.toJson(dupQso.qso))
            actionResult.addSad(s"Duplicate:\n$pretty")
          case Success(Added(qsoRecord)) =>
            actionResult.addHappy(s"Added:\n${qsoRecord.qso.callsign} ${qsoRecord.qso.exchange}") //        actionResult.happy(
            if (qsoRecord.qso.callsign == "WA9NNN") {
              onFX {
                statusPane.message(StatusMessage("Thanks for using fdcluster, from Dick Lieber WA9NNN", styleClasses = Seq("hiDick")))
              }
            }
        }
        onFX {
          actionResult.done()
          clear()
        }
      }
    }
  }

  private def clear(): Unit = {
    qsoCallsign.reset()
    qsoClass.reset()
    qsoSection.reset()
    qsoCallsign.requestFocus()
  }

  def readQso(): Qso = {
    val exchange = Exchange(qsoClass.text.value, qsoSection.text.value)
    model.Qso(qsoCallsign.text.value, bandModeStore.bandModeOperator.value, exchange)
  }

  /**
   *
   * @param nextText    what start off next field with.
   * @param destination the next field.
   */
  def nextField(nextText: String, destination: TextField): Unit = {
    destination.requestFocus()
    //    destination.setText(nextText) todo I don't understand how the class field works without this! Uncomment doubles number!
    destination.positionCaret(1)
  }

  clear()
}

