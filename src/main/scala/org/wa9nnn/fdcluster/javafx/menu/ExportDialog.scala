
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

