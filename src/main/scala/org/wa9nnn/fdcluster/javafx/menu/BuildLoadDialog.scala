
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
import java.text.NumberFormat
import javax.inject.Inject

case class BuildLoadRequest(path: String = System.getProperty("user.home"), max: Int = 100000)

class BuildLoadDialog @Inject()(persistence: Persistence) extends Dialog[BuildLoadRequest] {
  val blrIn: BuildLoadRequest = persistence.loadFromFile[BuildLoadRequest].getOrElse(BuildLoadRequest())
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

