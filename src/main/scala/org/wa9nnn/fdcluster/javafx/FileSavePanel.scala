
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.model.ExportFile
import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.GridPane
import scalafx.stage.{DirectoryChooser, Window}

import java.io.File

trait exportFile {

}

class FileSavePanel(exportFile: ExportFile )(implicit ownerWindow: Window) extends GridPane {

  val path: StringProperty = new StringProperty(exportFile.absoluteDirectory)
  val fileName = new StringProperty(exportFile.fileName)

  def result: ExportFile = new ExportFile(path.value, fileName.value)

  val pathDisplay: TextField = new TextField() {
    prefColumnCount = 25
    text <==> path
  }
  val fileNameField: TextField = new TextField() {
    text <==> fileName
  }

  val chooseFileButton: Button = new Button("choose file") {
    onAction = { e: ActionEvent =>
      val file: File = directoryChooser.showDialog(ownerWindow)
      path.value = file.getAbsoluteFile.toString
    }
  }
  val directoryChooser: DirectoryChooser = new DirectoryChooser {
    title = "Directory"
  }
  add(new Label("Directory:"), 0, 0)
  add(pathDisplay, 1, 0)
  add(chooseFileButton, 2, 0)
  add(new Label("File Name:"), 0, 1)
  add(fileNameField, 1, 1)
}

