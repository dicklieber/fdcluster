
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
import _root_.scalafx.Includes._
import _root_.scalafx.beans.property.StringProperty
import _root_.scalafx.event.ActionEvent
import _root_.scalafx.geometry.Insets
import _root_.scalafx.scene.control._
import _root_.scalafx.scene.layout.GridPane
import _root_.scalafx.stage.FileChooser
import _root_.scalafx.stage.FileChooser.ExtensionFilter

import java.io.File
import java.text.NumberFormat
import javax.inject.Inject

case class BuildLoadRequest(path: String = System.getProperty("user.home"), max: Int = 100000)

class BuildLoadDialog @Inject()(persistence: Persistence) extends Dialog[BuildLoadRequest] {
  val blrIn: BuildLoadRequest = persistence.loadFromFile[BuildLoadRequest](() => BuildLoadRequest())
  title = "Demo Data Bulk Loader"
  headerText = "Look, a Custom Login Dialog"
  val path: StringProperty = new StringProperty(blrIn.path)

  resultConverter = dialogButton => {
    val r = if (dialogButton == ButtonType.OK) {
      val nMx = try {
        val value = maxQsos.text.value
        NumberFormat.getInstance.parse(value).intValue()
      } catch {
        case e: NumberFormatException =>
          100000
      }
      BuildLoadRequest(path.value, nMx)
    }
    else
      null
    persistence.saveToFile(r)
    r
  }

  val pathDisplay: TextField = new TextField() {
    text <==> path
  }

  val maxQsos = new TextField()

  maxQsos.text.value = f"${blrIn.max}%,d"
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
      add(new Label("Max QSOs:"), 0, 1)
      add(maxQsos, 1, 1)
    }
  }
  val dp: DialogPane = dialogPane()

  dp.getButtonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  val fileChooser: FileChooser = new FileChooser {
    title = "Open Resource File"
    extensionFilters ++= Seq(
      new ExtensionFilter("csv", "*.csv"),
      new ExtensionFilter("Text Files", "*.txt")
    )
  }
}

