
package org.wa9nnn.fdlog.javafx

import javafx.scene.control.TextFormatter
import javafx.scene.input.KeyEvent
import javafx.scene.{control ⇒ jfxsc}
import org.wa9nnn.fdlog.model.{Exchange, Qso, StationContext}
import scalafx.application.Platform
class FDLogEntryController(scene: FDLogEntryScene) {
  val sections = new Sections
//  @jfxf.FXML
//  var qsoCallsign: jfxsc.TextField = _
//  @jfxf.FXML
//  var qsoClass: jfxsc.TextField = _
//  @jfxf.FXML
//  var qsoSection: jfxsc.TextField = _
//  @jfxf.FXML
//  var sectionPrompt: jfxsc.TextArea = _
//
//  //  @jfxf.FXML
//  var xyzzID: jfxsl.BorderPane = _

  /**
    * todo: inject somehow
    */
  implicit var stationContext: StationContext = _

  def injectContext(stationContext: StationContext): Unit = {
    this.stationContext = stationContext
  }

  //  val self = this

//  override def initialize(location: URL, resources: ResourceBundle): Unit = {

    forceCaps(scene.qsoCallsign)
    forceCaps(scene.qsoClass)
    forceCaps(scene.qsoSection)


    scene.qsoCallsign.addEventFilter(KeyEvent.KEY_TYPED,
      (event: KeyEvent) => {
        val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
        if (event.getCharacter.charAt(0).isDigit && ContestCallsign.valid(current)) {
          nextField(event, scene.qsoClass)
        }
      }
    )
    scene.qsoClass.addEventFilter(KeyEvent.KEY_TYPED,
      (event: KeyEvent) => {
        val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
        if (ContestClass.valid(current)) {
          nextField(event, scene.qsoSection)
        }
      }
    )

    scene.qsoSection.addEventFilter(KeyEvent.KEY_TYPED, (event: KeyEvent) ⇒ {
println(event.toString)
      Platform.runLater {
        println("Its later")
        val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
        val choices = sections
          .find(current)
          .map(section ⇒ section.section + ": " + section.name)
          .mkString("\n")
        scene.sectionPrompt.setText(choices)
      }

    })

//  private val property = scene.qsoSection.onAction = (a: ActionEvent) => {
//      val str = a.toString()
//      println(str)
////      val str = text()
////      val message = converter.fromString(str) + "\n"
////      outputTextArea.text = message + outputTextArea.text()
////      text() = ""
//    }
//  }


  def save(): Unit = {

  println("save")
    val potentialQso = readQso()

    stationContext.store.add(potentialQso) foreach { dup ⇒ println("Dup: " + dup) }

    //handle dup
    scene.qsoCallsign.clear()
    scene.qsoClass.clear()
    scene.qsoSection.clear()
    scene.qsoCallsign.requestFocus()

  }

  def readQso(): Qso = {
    //    start with our station and just replace the callsign with the worked sttion
    val station = stationContext.station.copy(callsign = scene.qsoCallsign.getText)
    val exchange = Exchange(scene.qsoClass.getText, scene.qsoSection.getText)
    Qso(station, exchange)
  }


  private def nextField(event: KeyEvent, destination: jfxsc.TextField): Unit = {
    event.consume()
    destination.requestFocus()
    destination.setText(event.getCharacter)
    destination.positionCaret(1)

  }


  private[javafx] def forceCaps(textField: jfxsc.TextField): Unit = {
    textField.setTextFormatter(new TextFormatter[AnyRef]((change: TextFormatter.Change) => {
      def foo(change: TextFormatter.Change) = {
        change.setText(change.getText.toUpperCase)
        change
      }

      foo(change)
    }))
  }

}
