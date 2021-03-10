
package org.wa9nnn.fdcluster.javafx.entry

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import org.wa9nnn.fdcluster.javafx.{CallSignField, ClassField, SectionField}
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
import scala.concurrent.Await

/**
 * Create ScalaFX UI for field day entry mode.
 */
class EntryScene @Inject()(@Inject()
                           bandModeStore: BandModeOperatorStore,
                           bandModeOpPanel: BandModeOpPanel,
                           @Inject() @Named("store") store: ActorRef) {
  private implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  val qsoCallsign = new CallSignField()
  val qsoClass = new ClassField()

  val qsoSection = new SectionField()

  var actionResult = new TextArea() with WithDisposition
  actionResult.getStyleClass.add("dupPrompt")
  actionResult.disable

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
        actionResult
      ),
      new VBox(
        new Label("Class"),
        qsoClass,
        new VBox(
          buttons,
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
    nextField(next, qsoClass)
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

    val future = store ? potentialQso
    Await.result(future, timeout.duration).asInstanceOf[AddResult] match {
      case Dup(dupQso) ⇒
        val pretty = Json.prettyPrint(Json.toJson(dupQso.qso))
        actionResult.text = s"Duplicate:\n$pretty"
        actionResult.sad()
      case Added(qsoRecord) ⇒
        actionResult.text = s"Added:\n${qsoRecord.qso.callsign} ${qsoRecord.qso.exchange}"
        actionResult.happy()
    }

    clear()
  }

  private def clear(): Unit = {
    actionResult.clear()
    qsoCallsign.reset()
    qsoClass.reset()
    qsoSection.reset()
    qsoCallsign.requestFocus()
  }

  def readQso(): Qso = {
    val exchange = Exchange(qsoClass.text.value, qsoSection.text.value)
    model.Qso(qsoCallsign.text.value, bandModeStore.bandModeOperator, exchange)
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

