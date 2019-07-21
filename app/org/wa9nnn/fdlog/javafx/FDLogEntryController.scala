
package org.wa9nnn.fdlog.javafx

import javafx.scene.control.TextFormatter
import javafx.scene.input.KeyEvent
import javafx.scene.{control ⇒ jfxsc}
import org.wa9nnn.fdlog.model.{Exchange, Qso, StationContext}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.css.Styleable
import scalafx.event.ActionEvent
import scalafx.scene.control.TextField

class FDLogEntryController(scene: FDLogEntryScene, initialStationContext: StationContext) {

  implicit var stationContext: StationContext = initialStationContext
  val qsoSection = new StringProperty()
  private val qsoCallsignText: StringProperty = scene.qsoCallsign.textProperty()
  private val qsoClassText: StringProperty = scene.qsoClass.textProperty()
  private val qsoSectionText: StringProperty = scene.qsoSection.textProperty()


  private val allSections = Sections.sections.map { section: Section ⇒ f"${section.code}%-3s" }
    .grouped(10)
    .map(_.mkString(" "))
    .mkString("\n")

  scene.sectionPrompt.setText(allSections)
  qsoSectionText.onChange { (_, _, newValue) =>
    validateQso()
    val choices = if (newValue != "") {
      Sections
        .find(newValue)
        .map(section ⇒ section.code + ": " + section.name)
        .mkString("\n")
    } else {
      allSections
    }
    scene.sectionPrompt.setText(choices)
  }

  forceCaps(scene.qsoCallsign)
  forceCaps(scene.qsoClass)
  forceCaps(scene.qsoSection)


  scene.qsoCallsign.addEventFilter(KeyEvent.KEY_TYPED,
    (event: KeyEvent) => {
      val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
      val character = event.getCharacter
      if (character.isDefinedAt(0) && character.charAt(0).isDigit && ContestCallsign.valid(current)) {
        nextField(event, scene.qsoClass)
      }

      Platform.runLater {
        validateQso()
      }
    }
  )
  scene.qsoClass.addEventFilter(KeyEvent.KEY_TYPED,
    (event: KeyEvent) => {
      val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
      if (ContestClass.valid(current)) {
        nextField(event, scene.qsoSection)
      }
      Platform.runLater {
        validateQso()
      }
    }
  )

  scene.qsoSection.addEventFilter(KeyEvent.KEY_TYPED, (_: KeyEvent) ⇒ {
    Platform.runLater {
      validateQso()
    }
  }
  )

  scene.qsoSection.addEventFilter(KeyEvent.KEY_TYPED,
    (event: KeyEvent) ⇒ {
      if (event.getCharacter == "\r" && !scene.qsoSubmit.isDisabled) {
        save()
      }
    }
  )

  scene.qsoSubmit.onAction = (_: ActionEvent) => {
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

    if (validateField(scene.qsoCallsign, ContestCallsign) &&
      validateField(scene.qsoClass, ContestClass) &&
      validateField(scene.qsoSection, ContestSection)) {
      scene.qsoSubmit.disable = false
      makeHappy(scene.qsoSubmit)
    }
    else {
      scene.qsoSubmit.disable = true
      makeSad(scene.qsoSubmit)
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

  def save(): Unit = {
    val potentialQso = readQso()

    stationContext.store.add(potentialQso) foreach { dup ⇒ println("Dup: " + dup) }

    //handle dup
    scene.qsoCallsign.clear()
    scene.qsoClass.clear()
    //    qsoSection.value = ""
    scene.qsoSection.clear()
    scene.qsoCallsign.requestFocus()

  }

  def readQso(): Qso = {
    val exchange = Exchange(qsoClassText.value, qsoSectionText.value)

    Qso(qsoCallsignText.value, stationContext.bandMode, exchange)
  }

  private def nextField(event: KeyEvent, destination: TextField): Unit = {
    event.consume() // we dont want this for this control
    destination.requestFocus()
    destination.setText(event.getCharacter)
    destination.positionCaret(1)

  }


  private[javafx] def forceCaps(textField: TextField): Unit = {
    textField.setTextFormatter(new TextFormatter[AnyRef]((change: TextFormatter.Change) => {
      def foo(change: TextFormatter.Change) = {
        change.setText(change.getText.toUpperCase)
        change
      }

      foo(change)
    }))
  }
}
