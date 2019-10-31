
package org.wa9nnn.fdcluster.javafx.debug

import akka.actor.ActorRef
import com.google.inject.name.Named
import javax.inject.Inject
import org.wa9nnn.fdcluster.store.DebugKillRandom
import scalafx.scene.control.TextInputDialog

class DebugRemoveDialog @Inject()(@Named("store") store: ActorRef) extends TextInputDialog("1") {
  title = "Debug Random QSO Killer"
  headerText = "Randomly remove some QSOs from this node."
  contentText = "Number top kill:"

  def apply(): Unit = {

    val result = showAndWait()
    result foreach { nToKill ⇒
       store ! DebugKillRandom(nToKill.toInt)
    }
  }
}