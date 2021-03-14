
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

package org.wa9nnn.fdcluster.javafx.menu

import javafx.stage.Window
import org.wa9nnn.fdcluster.javafx.FileSavePanel
import org.wa9nnn.fdcluster.model.AdifExportRequest
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence
import scalafx.Includes._
import scalafx.scene.control._
import scalafx.stage.DirectoryChooser

import javax.inject.Inject

class ExportDialog @Inject()(persistence: Persistence) extends Dialog[AdifExportRequest] {
  val exportRequest: AdifExportRequest = persistence.loadFromFile[AdifExportRequest].getOrElse(AdifExportRequest())
  private val dp: DialogPane = dialogPane()
  implicit val win: Window = dp.getScene.getWindow

  private val fileSavePanel = new FileSavePanel(exportRequest.exportFile)(win)

  title = "Export"
  headerText = "Save as ADIF (adi)"
  resultConverter = dialogButton => {
     if (dialogButton == ButtonType.OK) {

       val exportRequest = AdifExportRequest(fileSavePanel.result)
       persistence.saveToFile(exportRequest)
       exportRequest

    }
    else
      null
  }

  dialogPane().setContent(fileSavePanel)

  dp.getButtonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  val directoryChooser: DirectoryChooser = new DirectoryChooser {
    title = "Directory"
  }
}

