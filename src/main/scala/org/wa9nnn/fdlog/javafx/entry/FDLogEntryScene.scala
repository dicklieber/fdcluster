
package org.wa9nnn.fdlog.javafx.entry

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import javafx.scene.input.KeyEvent
import javafx.scene.{control ⇒ jfxsc}
import org.wa9nnn.fdlog.javafx._
import org.wa9nnn.fdlog.model.{CurrentStationProvider, Exchange, Qso}
import org.wa9nnn.fdlog.store.{AddResult, Added, Dup}
import play.api.libs.json.Json
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.css.Styleable
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox, VBox}

import scala.concurrent.Await

/**
 * Create JavaFX UI for field day entry mode.
 */
class FDLogEntryScene @Inject()(@Inject() currentStationProvider: CurrentStationProvider,
                                @Inject() @Named("store") store: ActorRef) {
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  val qsoCallsign: TextField = new TextField() {
    styleClass.append("sadQso")
  }
  val qsoClass: TextField = new TextField() {
    styleClass.append("sadQso")
  }
  val qsoSection: TextField = new TextField()
  qsoSection.getStyleClass.add("sadQso")

  var sectionPrompt = new TextArea()
  sectionPrompt.getStyleClass.add("sectionPrompt")
  sectionPrompt.disable
  var actionResult = new TextArea()
  actionResult.getStyleClass.add("dupPrompt")
  actionResult.disable

  val qsoSubmit = new Button("Log")
  qsoSubmit.disable = true
  qsoSubmit.getStyleClass.add("sadQso")
  val pane: BorderPane = new BorderPane {
    padding = Insets(25)
    center = new HBox(
      new VBox(
        new Label("Callsign"),
        qsoCallsign,
        actionResult
      ),
      new VBox(
        new Label("Class"),
        qsoClass,
        qsoSubmit
      ),
      new VBox(
        new Label("Section"),
        qsoSection,
        sectionPrompt
      )
    )
  }

  val scene: Scene = new Scene {
    root = pane
  }

  private val qsoCallsignText = qsoCallsign.textProperty()
  private val qsoClassText = qsoClass.textProperty()
  private val qsoSectionText = qsoSection.textProperty()


  private val allSections = Sections.sections.map { section: Section ⇒ f"${section.code}%-3s" }
    .grouped(10)
    .map(_.mkString(" "))
    .mkString("\n")

  sectionPrompt.setText(allSections)
  qsoSectionText.addListener { (_, _, newValue) =>
    validateQso()
    val choices = if (newValue != "") {
      Sections
        .find(newValue)
        .map(section ⇒ section.code + ": " + section.name)
        .mkString("\n")
    } else {
      allSections
    }
    sectionPrompt.setText(choices)
  }

  forceCaps(qsoCallsign)
  forceCaps(qsoClass)
  forceCaps(qsoSection)


  qsoCallsign.addEventFilter(KeyEvent.KEY_TYPED,
    (event: KeyEvent) => {
      val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
      val character = event.getCharacter
      if (character.isDefinedAt(0) && character.charAt(0).isDigit && ContestCallsign.valid(current)) {
        nextField(event, qsoClass)
      }
      if (current.isEmpty) {
        actionResult.clear()
      }

      Platform.runLater {
        validateQso()
      }
    }
  )
  qsoClass.addEventFilter(KeyEvent.KEY_TYPED,
    (event: KeyEvent) => {
      val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
      if (ContestClass.valid(current)) {
        nextField(event, qsoSection)
      }
      Platform.runLater {
        validateQso()
      }
    }
  )

  qsoSection.addEventFilter(KeyEvent.KEY_TYPED,
    (event: KeyEvent) ⇒ {
      if (event.getCharacter == "\r" && !qsoSubmit.isDisabled) {
        save()
      }
      Platform.runLater {
        validateQso()
      }
    }
  )

  qsoSubmit.onAction = (_: ActionEvent) => {
    save()
  }

  def validateQso(): Unit = {

    def validateField(destination: TextField, validField: FieldValidator): Boolean = {
      if (validField.valid(destination)) {
        makeHappy(destination)
      } else {
        makeSad(destination)
      }
    }

    if (validateField(qsoCallsign, ContestCallsign) &&
      validateField(qsoClass, ContestClass) &&
      validateField(qsoSection, ContestSection)) {
      qsoSubmit.disable = false
      makeHappy(qsoSubmit)
    }
    else {
      qsoSubmit.disable = true
      makeSad(qsoSubmit)
    }
  }

  def makeHappy(destination: Styleable): Boolean = {
    destination.styleClass.replaceAll("sadQso", "happyQso")
    true
  }

  def makeSad(destination: Styleable): Boolean = {
    destination.styleClass.replaceAll("happyQso", "sadQso")
    false
  }

  def showSad(destination: TextInputControl, message: String): Unit = {
    makeSad(destination)
    destination.setText(message)
  }

  def showHappy(destination: TextInputControl, message: String): Unit = {
    makeHappy(destination)
    destination.setText(message)
  }

  def save(): Unit = {
    import org.wa9nnn.fdlog.model.MessageFormats._
    val potentialQso = readQso()

    val future = store ? potentialQso
    Await.result(future, timeout.duration).asInstanceOf[AddResult] match {
      case Dup(dupQso) ⇒
        val pretty = Json.prettyPrint(Json.toJson(dupQso.qso))
        showSad(actionResult, s"Duplicate:\n$pretty")
      case Added(qsoRecord) ⇒
        showHappy(actionResult,s"Added:\n${qsoRecord.qso.callsign} ${qsoRecord.qso.exchange}")
    }

    qsoCallsign.clear()
    qsoClass.clear()
    //    qsoSection.value = ""
    qsoSection.clear()
    qsoCallsign.requestFocus()
  }

  def readQso(): Qso = {
    val exchange = Exchange(qsoClassText.get(), qsoSectionText.get())

    Qso(qsoCallsignText.get(), currentStationProvider.currentStation.bandMode, exchange)
  }

  def nextField(event: KeyEvent, destination: TextField): Unit = {
    event.consume() // we dont want this for this control
    destination.requestFocus()
    destination.setText(event.getCharacter)
    destination.positionCaret(1)

  }

  def forceCaps(textField: TextField): Unit = {
    textField.setTextFormatter(new TextFormatter[AnyRef]((change: TextFormatter.Change) => {
      def foo(change: TextFormatter.Change) = {
        change.setText(change.getText.toUpperCase)
        change
      }

      foo(change)
    }))
  }


}

