
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