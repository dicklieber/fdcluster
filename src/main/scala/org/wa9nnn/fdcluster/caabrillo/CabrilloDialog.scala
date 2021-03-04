
package org.wa9nnn.fdcluster.caabrillo

import javafx.scene.control.DialogPane
import org.wa9nnn.fdcluster.model.{AvailableBand, BandModeFactory}
import org.wa9nnn.util.Persistence
import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control.{TextArea => fxTextArea}
import scalafx.scene.control._
import scalafx.scene.layout.GridPane

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

case class CabrilloData(fields: Seq[Field])

trait Field extends StringProperty {
  def name: String
  val cabName: String
  val control: Control

  def value: String

  def setInGrid(gridPane: GridPane)(implicit row: AtomicInteger): Unit = {
    val r = row.getAndIncrement()
    gridPane.add(new Label(name + ":"), 0, r)
    gridPane.add(control, 1, r)
  }
}

case class Text(override val name: String, val cabName: String) extends Field {
  val control = new TextField()
  this <==> control.text
}
case class TextArea(override val name: String, val cabName: String) extends Field {
  val control = new fxTextArea()
  control.setPrefRowCount(2)
  control.setPrefColumnCount(25 )
  this <==> control.text
}

case class Combo(override val name: String, val cabName: String, choices: String*) extends Field {
  private val defaultIndicator = "+"
  private val defVal = choices.find(_.startsWith(defaultIndicator)).map(_.tail)
  private val fixed = choices.map(_.dropWhile(_ == defaultIndicator.head))
  val control = new ComboBox[String](fixed)
  private val defChoice: String = defVal.getOrElse(fixed.head)
  control.selectionModel.value.select(defChoice)
  //todo bind selected value
}

class CabrilloDialog @Inject()(persistence: Persistence,
                               bandModeFactory: BandModeFactory
                              ) extends Dialog[CabrilloData] {
  //  val exportRequest: ExportRequest = persistence.loadFromFile[ExportRequest].getOrElse(ExportRequest())


  title = "Cabrillo Export"
  headerText = "Caabrillo Header Information"

  val fields: Seq[Field] = Seq(
    Combo("Operator", "CATEGORY-OPERATOR", "+SINGLE-OP", "MULTI-OP", "CHECKLOG"),
    Combo("Station", "CATEGORY-STATION", "DISTRIBUTED", "+FIXED", "MOBILE", "PORTABLE", "ROVER", "ROVER-LIMITED", "ROVER-UNLIMITED", "EXPEDITION", "HQ", "SCHOOL"),
    Combo("Transmitter", "CATEGORY-TRANSMITTER", "ONE", "TWO", "LIMITED", "+UNLIMITED", "SWL"),
    Combo("Power", "CATEGORY-POWER", "HIGH", "+LOW", "QRP"),
    Text("Club", "CLUB"),
    Combo("Assisted", "CATEGORY-ASSISTED", "ASSISTED", "+NON-ASSISTED"),
//    LabeledCombo("Band", "CATEGORY-BAND", "All" +: bandModeFactory.avalableBands.map(_.band):_*),
//    LabeledCombo("Mode", "CATEGORY-MODE", "MIXED",// wfd/fd
    Text("Operators", "OPERATORS"),
    Text("Name", "NAME"),
    TextArea("Address", "ADDRESS"),
    Text("City", "ADDRESS-CITY"),
    Text("State/Prov", "ADDRESS-STATE-PROVINCE"),
    Text("Zip/Post Code", "ADDRESS-POSTALCODE"),
    Text("Country", "ADDRESS-COUNTRY"),
  )


  val chooseFileButton: Button = new Button("choose file") {
    onAction = { e: ActionEvent =>
      //      val file: File = directoryChooser.showDialog(dp.getScene.getWindow)
      //      path.value = file.getAbsoluteFile.toString
    }
  }

  dialogPane().setContent {
    new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)
      implicit val row = new AtomicInteger()
      fields.foreach(f =>
        f.setInGrid(this)
      )
    }
  }
  val dp: DialogPane = dialogPane()

  dp.getButtonTypes.addAll(ButtonType.OK, ButtonType.Cancel)
}


