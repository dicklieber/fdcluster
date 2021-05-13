
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

package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.model.ExportFile
import _root_.scalafx.Includes._
import _root_.scalafx.beans.property.StringProperty
import _root_.scalafx.event.ActionEvent
import _root_.scalafx.scene.control.{Button, Label, TextField}
import _root_.scalafx.scene.layout.GridPane
import _root_.scalafx.stage.{DirectoryChooser, Window}

import java.io.File

trait exportFile {

}

class FileSavePanel(exportFile: ExportFile)(implicit ownerWindow: Window) extends GridPane {

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

  val chooseFileButton: Button = new Button("choose directory") {
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

