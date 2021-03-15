
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

package org.wa9nnn.fdcluster.tools

import org.wa9nnn.fdcluster.javafx.DialogGrid
import org.wa9nnn.util.StructuredLogging
import scalafx.scene.control.{ButtonType, Dialog}

class RandomQsoDialog(generateRandomQsos: GenerateRandomQsos = GenerateRandomQsos()) extends Dialog[GenerateRandomQsos] with StructuredLogging {
  title = "Random Qso Generator"
  headerText = "Create a contests worth of QSOs"


  val dp = dialogPane()
  private val dialogGrid = new DialogGrid()
  dp.setContent(dialogGrid)
  val ntoGen = dialogGrid.addInt("Qso count", generateRandomQsos.ntoGen)
  val hoursBefore = dialogGrid.addInt("Hours before", generateRandomQsos.hoursBefore)
  val between = dialogGrid.addDuration("Duration between", generateRandomQsos.between)

  dp.getButtonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  resultConverter = dialogButton => {
    if (dialogButton == ButtonType.OK) {

      GenerateRandomQsos(ntoGen.value, hoursBefore.value, between.value)
    }
    else
      null
  }

}
