
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

import javafx.scene.control.DialogPane
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence
import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.File
import javax.inject.Inject

case class ImportRequest(directory: String = System.getProperty("user.home"))

class ImportDialog @Inject()(persistence: Persistence) extends Dialog[ImportRequest] {
  val blrIn: ImportRequest = persistence.loadFromFile[ImportRequest].getOrElse(ImportRequest())
  title = "Import"
  headerText = "Load ADIF"
  val path: StringProperty = new StringProperty(blrIn.directory)

  resultConverter = dialogButton => {
    val r = if (dialogButton == ButtonType.OK) {
      val rr = ImportRequest(path.value)
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

  val chooseFileButton: Button = new Button("choose file") {
    onAction = { e: ActionEvent =>
      val file: File = fileChooser.showOpenDialog(dp.getScene.getWindow)
      path.value = file.getAbsoluteFile.toString
    }
  }

  dialogPane().setContent {
    new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      add(new Label("File:"), 0, 0)
      add(pathDisplay, 1, 0)
      add(chooseFileButton, 2, 0)
    }
  }
  val dp: DialogPane = dialogPane()

  dp.getButtonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  val fileChooser: FileChooser = new FileChooser {
    title = "Open Resource File"
    extensionFilters ++= Seq(
      new ExtensionFilter("ADIF", Seq("*.adif", "*.adi")),
      new ExtensionFilter("Cabrillo", Seq("*.cab")),
      new ExtensionFilter("Text Files", "*.txt"),
      new ExtensionFilter("Any", "*.*")
    )
  }
}

