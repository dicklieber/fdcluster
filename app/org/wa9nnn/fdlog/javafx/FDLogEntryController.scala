
package org.wa9nnn.fdlog.javafx

import java.net.URL
import java.util

import javafx.scene.{control ⇒ jfxsc}
import javafx.scene.{layout ⇒ jfxsl}
import javafx.{fxml ⇒ jfxf}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.layout.{BorderPane, GridPane}

class FDLogEntryController {
  //class FDLogEntryController extends jfxf.Initializable {
  println("Hello")
  @jfxf.FXML
  var qsoCallsign: jfxsc.TextField = _
  @jfxf.FXML
  var qsoClass: jfxsc.TextField = _
  @jfxf.FXML
  var qsoSection: jfxsc.TextField = _

  @jfxf.FXML
  var xyzzID: jfxsl.BorderPane = _


  @jfxf.FXML
  def onKeyReleased(event: ActionEvent): Unit = {
    println(event)
  }
}
