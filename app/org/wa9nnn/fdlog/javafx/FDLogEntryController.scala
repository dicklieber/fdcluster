
package org.wa9nnn.fdlog.javafx

import java.net.URL
import java.util

import javafx.scene.{control ⇒ jfxsc}
import javafx.scene.{layout ⇒ jfxsl}
import javafx.{event ⇒ jfxe}
import javafx.{event ⇒ jfxe, geometry ⇒ jfxg, scene ⇒ jfxs, util ⇒ jfxu}
import javafx.scene.{effect ⇒ jfxse, input ⇒ jfxsi, layout ⇒ jfxsl, transform ⇒ jfxst}
import javafx.{fxml ⇒ jfxf}
import scalafx.Includes._
import scalafx.event.subscriptions.Subscription
import scalafx.event.{ActionEvent, EventHandlerDelegate}
import scalafx.scene.control.TextField
import scalafx.scene.{Group, Node}
import scalafx.scene.input.{KeyEvent, MouseEvent}
import scalafx.scene.layout.{BorderPane, GridPane}

class FDLogEntryController {
  //class FDLogEntryController extends jfxf.Initializable {
  println("Hello")
//  @jfxf.FXML
  var qsoCallsign: jfxsc.TextField = _
//  @jfxf.FXML
  var qsoClass: jfxsc.TextField = _
//  @jfxf.FXML
  var qsoSection: jfxsc.TextField = _

  @jfxf.FXML
  var xyzzID: jfxsl.BorderPane = _


//  qsoCallsign.addEventFilter(javafx.scene.input.KeyEvent.KEY_TYPED,  )

//  qsoCallsign.filterEvent(KeyEvent.Any) { ke :KeyEvent⇒
//    ke
//  }

//  @jfxf.FXML
//  qsoCallsign.onKeyReleased(e:jfxe.EventHandler[_ >: jfxsi.KeyEvent]) = {
//
//  }
//def onKeyReleased(keyEvent: KeyEvent): Unit = {
//  val text = keyEvent.getText
//  val character = keyEvent.getCharacter.charAt(0)
//  System.out.println(keyEvent)
//  val current = qsoCallsign.getText
//  val mayBeCallsign = Callsign.isCallsign(current)
//  if (Character.isDigit(character)) System.out.println("mayBeCallsign = " + mayBeCallsign)
//}
//




}
