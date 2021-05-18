
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

import _root_.scalafx.Includes._
import _root_.scalafx.beans.binding.{Bindings, ObjectBinding}
import _root_.scalafx.beans.property.ObjectProperty
import _root_.scalafx.event.ActionEvent
import _root_.scalafx.geometry.{Insets, Pos}
import _root_.scalafx.scene.control._
import _root_.scalafx.scene.layout.{BorderPane, HBox, Pane, VBox}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Named
import com.google.inject.{Inject, Injector}
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.contest.{JournalProperty, OkToLogGate}
import org.wa9nnn.fdcluster.javafx.entry.section.SectionField
import org.wa9nnn.fdcluster.javafx.{CallSignField, ClassField, StatusMessage, StatusPane}
import org.wa9nnn.fdcluster.model
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.{AddResult, Added, Dup, FailedToAdd}
import org.wa9nnn.util.WithDisposition
import play.api.libs.json.Json

import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Create ScalaFX UI for field day entry mode.
 */
@Singleton
class EntryTab @Inject()(injector: Injector,
                         currentStationPanel: CurrentStationPanel,
                         contestProperty: ContestProperty,
                         nodeAddress: NodeAddress,
                         classField: ClassField,
                         @Named("qsoMetadata") qsoMetadataProperty: ObjectProperty[QsoMetadata],
                         currentStationProperty: CurrentStationProperty,
                         statsPane: StatsPane,
                         statusPane: StatusPane,
                         journalManager: JournalProperty,
                         @Named("store") store: ActorRef,
                         okToLogGate: OkToLogGate,
                        ) extends Tab with LazyLogging {
  private implicit val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  text = "Entry"
  closable = false

  var actionResult: ActionResult = new ActionResult(store, qsoMetadataProperty.value)
  val callSignField: CallSignField = new CallSignField(actionResult) {
    styleClass += "qsoCallSign"
  }

  val qsoSection: SectionField = new SectionField() {
    styleClass += "qsoSection"
  }

  val qsoSubmit = new Button("Log") with WithDisposition
  val clearButton = new Button("Clear") with WithDisposition
  clearButton.onAction = _ => {
    clear()
  }
  qsoSubmit.disable = true
  qsoSubmit.sad()
  private val initialExchange = contestProperty.ourExchangeProperty
  val ourExchangeLabel: Label = new Label(initialExchange.value.display) {
    styleClass += "exchange"
  }
  val ourExchangeMnomicLabel: Label = new Label(initialExchange.value.mnomonics) {
    styleClass += "exchangeMnemonics"
  }
  contestProperty.ourExchangeProperty.onChange { (_, _, ex) =>
    onFX {
      ourExchangeLabel.text = ex.display
      ourExchangeMnomicLabel.text = ex.mnomonics
    }
  }


  val entryPane: BorderPane = new BorderPane {
    padding = Insets(25)
    private val buttons = new HBox() {
      alignment = Pos.BottomCenter
      spacing = 8
      padding = Insets(10)
      children = List(qsoSubmit, clearButton)
    }
    center = new HBox(
      new VBox(
        new Label("CallSign"),
        callSignField,
        actionResult.pane,
        statsPane.pane,
      ),
      new VBox(
        new Label("Class"),
        classField,
        new VBox(
          buttons,
          currentStationPanel.pane
        )
      ),
      new VBox(
        new Label("Section"),
        new HBox(qsoSection, new Label("We are: "), ourExchangeLabel, ourExchangeMnomicLabel),
        qsoSection.sectionPrompt
      )
    )
  }

  callSignField.onDone { next =>
    if (classField.text.value.isEmpty) {
      nextField(next, classField)
    }
  }
  classField.onDone { _ =>
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

  val allFields = new Compositor(callSignField.validProperty, classField.validProperty, qsoSection.validProperty)
  allFields.onChange { (_, _, state) =>
    if (state) {
      qsoSubmit.disable = false
      qsoSubmit.happy()
    } else {
      qsoSubmit.disable = true
      qsoSubmit.sad()
    }
  }


  content = setOk(okToLogGate.value)
  okToLogGate.onChange { (_, _, nv) =>
    onFX {
      content = setOk(nv)
    }
  }

  def setOk(ok: Boolean): Pane = {
    if (ok)
      entryPane
    else
      injector.instance[NotReadyPane]
  }

  def save(): Unit = {
    val potentialQso: Qso = readQso()
    if (potentialQso.callSign == contestProperty.callSign) {
      actionResult.showSad(s"Can't work our own station: \n${potentialQso.callSign}!")
    }
    else {
      val future: Future[AddResult] = (store ? potentialQso).mapTo[AddResult]
      future onComplete { tr: Try[AddResult] =>
        actionResult.clear()
        tr match {
          case Failure(exception) =>
            logger.error(s"adding QSO: $potentialQso", exception)
          case Success(Dup(dupQso)) =>
            actionResult.addSad(s"Duplicate:\n${dupQso.qso.callSign} ${dupQso.qso.bandMode}")
            logger.info(s"Dup: ${Json.toJson(dupQso.qso).toString()}")
          case Success(Added(qsoRecord)) =>
            actionResult.addHappy(s"Added:\n${qsoRecord.qso.callSign} ${qsoRecord.qso.exchange}") //        actionResult.happy(
            logger.info(s"Added: ${Json.toJson(qsoRecord).toString}")
            if (qsoRecord.qso.callSign == "WA9NNN") {
              onFX {
                statusPane.message(StatusMessage("Thanks for using fdcluster, from Dick Lieber WA9NNN", styleClasses = Seq("hiDick")))
              }
            }
          case Success(FailedToAdd(exception)) =>
            actionResult.addSad(s"Failed to add QSO! Err: ${exception.getMessage}")
            logger.error(s"Failed to ad QSO", exception)

        }
        onFX {
          actionResult.done()
          clear()
        }
      }
    }
  }

  private def clear(): Unit = {
    callSignField.reset()
    classField.reset()
    qsoSection.reset()
    callSignField.requestFocus()
  }

  def readQso(): Qso = {
    val exchange = Exchange(classField.text.value, qsoSection.text.value)
    model.Qso(callSignField.text.value, exchange, currentStationProperty.bandMode)
  }

  /**
   *
   * @param nextText    what start off next field with.
   * @param destination the next field.
   */
  def nextField(nextText: String, destination: TextField): Unit = {
    destination.requestFocus()
    destination.positionCaret(1)
  }

  clear()

  val qsoMetadataBinding: ObjectBinding[QsoMetadata] = Bindings.createObjectBinding(() => {
    val cs = currentStationProperty.value
    QsoMetadata(operator = cs.operator,
      rig = cs.rig,
      ant = cs.antenna,
      node = nodeAddress.qsoNode,
      contestId = contestProperty.contest.qsoId
    )
  }, currentStationProperty, contestProperty)

  qsoMetadataProperty <== qsoMetadataBinding

}

