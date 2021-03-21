
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
import org.wa9nnn.fdcluster.FileManager
import org.wa9nnn.fdcluster.javafx.FileSavePanel
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{AdifExportRequest, ContestProperty}
import org.wa9nnn.util.Persistence
import scalafx.Includes._
import scalafx.scene.control._

import javax.inject.Inject

/**
 * Dialog to get directory and file where export an DIF file.
 *
 * @param persistence     saves user entered data between sessions.
 * @param fileManager     knows all about FDCluster files.
 * @param contestProperty so can make contest-specific files.
 */
class ExportDialog @Inject()(implicit
                             persistence: Persistence,
                             fileManager: FileManager,
                             contestProperty: ContestProperty) extends Dialog[AdifExportRequest] {
  private val adifExportRequest: AdifExportRequest =
    persistence.loadFromFile[AdifExportRequest] { () =>
      AdifExportRequest(fileManager.defaultExportFile("adif"))
    }

  private val dp: DialogPane = dialogPane()

  val win: Window = dp.getScene.getWindow
  private val fileSavePanel = new FileSavePanel(adifExportRequest.exportFile)(win)

  title = "Export"
  headerText = "Save as ADIF."
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
}

