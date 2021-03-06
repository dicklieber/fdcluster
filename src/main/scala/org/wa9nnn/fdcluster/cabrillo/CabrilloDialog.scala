
package org.wa9nnn.fdcluster.cabrillo

import javafx.scene.control.DialogPane
import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, ButtonType, Dialog, Label, TextField}
import scalafx.scene.layout.GridPane
import scalafx.stage.DirectoryChooser

import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class CabrilloDialog @Inject()(cabrilloValuesStore: CabrilloValuesStore,
                               cabrilloFieldsSource: CabrilloFieldsSource,
                              ) extends Dialog[CabrilloExportRequest] {
  private val cer: CabrilloExportRequest = cabrilloValuesStore.value
  implicit val savedValues: CabrilloValues = cer.cabrilloValues

  title = "Cabrillo Export"
  headerText = "Cabrillo Header and Export"

  val fields: Seq[Field] = cabrilloFieldsSource.cabrilloFields


  val path: StringProperty = new StringProperty(cer.directory)
  val fileName = new StringProperty(cer.fileName)

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

  val dp: DialogPane = dialogPane()
  dp.setContent {
    new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)
      implicit val row = new AtomicInteger()
      fields.foreach(f =>
        f.setInGridAndInit(this)
      )
      val dirRow = row.getAndIncrement()
      add(new Label("Directory:"), 0, dirRow)
      add(pathDisplay, 1, dirRow)
      add(chooseFileButton, 2, dirRow)
      val fileRow = row.getAndIncrement()

      add(new Label("File Name:"), 0, fileRow)
      add(fileNameField, 1, fileRow)

    }
  }


  val ButtonTypeSaveAndExport = new ButtonType("Save & Export")
  val ButtonTypeSave = new ButtonType("Save")
  dp.getButtonTypes.addAll(ButtonTypeSaveAndExport, ButtonTypeSave, ButtonType.Cancel)

  def buildCER: CabrilloExportRequest = {
    val vf = CabrilloValues(fields.map(_.result))
    val cer = CabrilloExportRequest(path.value, fileName.value, vf)
    cabrilloValuesStore.value = cer
    cer
  }
  resultConverter = {
    case ButtonType.Cancel =>
      null
    case ButtonTypeSave =>
      buildCER
      null
    case ButtonTypeSaveAndExport =>
      buildCER
  }
  val directoryChooser: DirectoryChooser = new DirectoryChooser {
    title = "Directory"
  }

}


