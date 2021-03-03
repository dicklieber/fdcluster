
package org.wa9nnn.fdcluster.javafx.menu

import javafx.scene.control.DialogPane
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence
import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.stage.{DirectoryChooser, FileChooser}
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.File
import javax.inject.Inject

case class ExportRequest(directory: String = System.getProperty("user.home"), fileName: String = "fd.adif")

class ExportDialog @Inject()(persistence: Persistence) extends Dialog[ExportRequest] {
  val exportRequest: ExportRequest = persistence.loadFromFile[ExportRequest].getOrElse(ExportRequest())
  title = "Export"
  headerText = "Save as ADIF (adi)"
  val path: StringProperty = new StringProperty(exportRequest.directory)
  val fileName = new StringProperty(exportRequest.fileName)
  resultConverter = dialogButton => {
    val r = if (dialogButton == ButtonType.OK) {

      val rr = ExportRequest(path.value, fileName.value)
      persistence.saveToFile(rr)
      rr
    }
    else
      null
    r
  }

  val pathDisplay: TextField = new TextField() {
    text <==> path
  }
  val fileNameField: TextField = new TextField() {
    text <==> fileName
  }


  val chooseFileButton: Button = new Button("choose file") {
    onAction = { e: ActionEvent =>
      val file: File = directoryChooser.showDialog(dp.getScene.getWindow)
      path.value = file.getAbsoluteFile.toString
    }
  }

  dialogPane().setContent {
    new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      add(new Label("Directory:"), 0, 0)
      add(pathDisplay, 1, 0)
      add(chooseFileButton, 2, 0)
      add(new Label("File Name:"), 0, 1)
      add(fileNameField, 1, 1)

    }
  }
  val dp: DialogPane = dialogPane()

  dp.getButtonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  val directoryChooser: DirectoryChooser = new DirectoryChooser {
    title = "Directory"
  }
}

