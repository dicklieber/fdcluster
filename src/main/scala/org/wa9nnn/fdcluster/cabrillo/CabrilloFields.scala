
package org.wa9nnn.fdcluster.cabrillo

import scalafx.beans.property.StringProperty
import scalafx.scene.control.{ComboBox, Control, Label, TextField, TextArea => fxTextArea}
import scalafx.scene.layout.GridPane

import java.util.concurrent.atomic.AtomicInteger

/**
 * Common for any labeled field in this dialog
 */
abstract class Field() {
  val control: Control
  val valueProperty: StringProperty
  val name: String
  val cabName: String

  def result: CabrilloValue = CabrilloValue(cabName, valueProperty.value)

  def setInGridAndInit(gridPane: GridPane)(implicit row: AtomicInteger): Unit = {
    try {
      savedValues.valueForName(cabName).foreach(
        valueProperty.setValue(_)
      )
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

    val r = row.getAndIncrement()
    gridPane.add(new Label(name + ":"), 0, r)
    gridPane.add(control, 1, r)
  }

  implicit val savedValues: CabrilloValues
}

case class Text(override val name: String, cabName: String)(implicit val savedValues: CabrilloValues) extends Field {
  val control = new TextField()
  val valueProperty: StringProperty = control.text
}

case class TextArea(override val name: String, cabName: String)(implicit val savedValues: CabrilloValues) extends Field {
  val control = new fxTextArea()
  control.setPrefRowCount(2)
  control.setPrefColumnCount(25)
  val valueProperty: StringProperty = control.text
}

case class Combo(override val name: String, cabName: String, choices: String*)(implicit val savedValues: CabrilloValues) extends Field {
  private val defaultIndicator = "+"
  private val defVal = choices.find(_.startsWith(defaultIndicator)).map(_.tail)
  private val fixed = choices.map(_.dropWhile(_ == defaultIndicator.head))
  val control = new ComboBox[String](fixed)
  private val defChoice: String = defVal.getOrElse(fixed.head)
  control.selectionModel.value.select(defChoice)
  val valueProperty = new StringProperty()
  valueProperty <==> control.value
}
