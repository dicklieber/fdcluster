
package org.wa9nnn.fdlog.javafx

import java.net.URL
import java.util.ResourceBundle

import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.Initializable
import javafx.scene.control.TextFormatter
import javafx.scene.input.KeyEvent
import javafx.scene.{Node, control ⇒ jfxsc, layout ⇒ jfxsl}
import javafx.{fxml ⇒ jfxf}
import javax.swing.event.{ChangeEvent, ChangeListener}
import org.wa9nnn.fdlog.model.{Exchange, Qso, StationContext}
import scalafx.scene.control.TextField
import scalafx.Includes._
class FDLogEntryController extends Initializable {
  val sections = new Sections
  @jfxf.FXML
  var qsoCallsign: jfxsc.TextField = _
  @jfxf.FXML
  var qsoClass: jfxsc.TextField = _
  @jfxf.FXML
  var qsoSection: jfxsc.TextField = _
  @jfxf.FXML
  var sectionPrompt: jfxsc.TextArea = _

  //  @jfxf.FXML
  var xyzzID: jfxsl.BorderPane = _

  /**
    * todo: inject somehow
    */
  implicit var stationContext: StationContext = _

  def injectContext(stationContext: StationContext): Unit = {
    this.stationContext = stationContext
  }

  //  val self = this

  override def initialize(location: URL, resources: ResourceBundle): Unit = {

    forceCaps(qsoCallsign)
    forceCaps(qsoClass)
    forceCaps(qsoSection)


    qsoCallsign.addEventFilter(KeyEvent.KEY_TYPED,
      (event: KeyEvent) => {
        val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
        if (event.getCharacter.charAt(0).isDigit && ContestCallsign.valid(current)) {
          nextField(event, qsoClass)
        }
      }
    )
    qsoClass.addEventFilter(KeyEvent.KEY_TYPED,
      (event: KeyEvent) => {
        val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
        if (ContestClass.valid(current)) {
          nextField(event, qsoSection)
        }
      }
    )

    qsoSection.addEventFilter(KeyEvent.KEY_TYPED, (event: KeyEvent) ⇒ {
      val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
      val choices = sections
        .find(current)
        .map(section ⇒ section.section + ": " + section.name)
        .mkString("\n")
      sectionPrompt.setText(choices)
    })

    qsoSection.textProperty().addListener(new ChangeListener[Node] {
      def changed(p1: ObservableValue[_ <: Node], p2: Node, p3: Node) {}
    })
  }


  def save(): Unit = {
    println("save")
    val potentialQso = readQso()

    stationContext.store.add(potentialQso) foreach { dup ⇒ println("Dup: " + dup) }

    //handle dup
    qsoCallsign.clear()
    qsoClass.clear()
    qsoSection.clear()
    qsoCallsign.requestFocus()

  }

  def readQso(): Qso = {
    //    start with our station and just replace the callsign with the worked sttion
    val station = stationContext.station.copy(callsign = qsoCallsign.getText)
    val exchange = Exchange(qsoClass.getText, qsoSection.getText)
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
