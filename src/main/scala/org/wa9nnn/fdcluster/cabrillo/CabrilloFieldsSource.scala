
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

package org.wa9nnn.fdcluster.cabrillo

import _root_.scalafx.beans.property.StringProperty
import _root_.scalafx.scene.control.{ComboBox, Control, Label, TextField, TextArea => fxTextArea}
import _root_.scalafx.scene.layout.GridPane
import com.github.andyglow.config._
import com.typesafe.config.Config
import org.wa9nnn.fdcluster.cabrillo.CabrilloFieldsSource._
import org.wa9nnn.fdcluster.model.{BandFactory, ModeFactory}
import org.wa9nnn.util.StructuredLogging

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class CabrilloFieldsSource @Inject()(config: Config,
                                     bandFactory: BandFactory,
                                     modeFactory: ModeFactory
                                    )extends StructuredLogging {

  private def parseChoices(choices: String): Seq[String] = {
    choices match
      {
        case "$bands" =>
          bandFactory.availableBands.map(_.band)
        case "$modes" =>
          modeFactory.modes
        case x =>
          x.split("""\s+""")
      }
    }

  def cabrilloFields(implicit savedValues: CabrilloValues): Seq[Field] = {
    val cc: Seq[String] = config.get[List[String]]("fdcluster.cabrillo.fields")

    cc.flatMap { line =>
      val maybe: Try[Field] = Try {
        line match {
          case text(display, cabName) =>
            Text(display, cabName)
          case textArea(display, cabName) =>
            TextArea(display, cabName)
          case combo(display, cabName, choices) =>
            Combo(display, cabName, parseChoices(choices))
        }
      }
      maybe match {
        case Failure(exception) =>
          logger.error(s"Error ${exception.getMessage} Input: $line")
          None
        case Success(value: Field) => Some(value)
      }
    }
  }
}

object CabrilloFieldsSource {
  val combo: Regex = """Combo:\s*(\w+)\s*([\-\w]+)\s*\[\s*(.*)\w*\]\s*""".r
  val textArea: Regex = """TextArea:\s*(\w+)\s*([\-\w]+)""".r
  val text: Regex = """Text:\s*([/\w]+)\s*([\-\w]+)""".r

}

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

case class Combo(override val name: String, cabName: String, choices: Seq[String])(implicit val savedValues: CabrilloValues) extends Field {
  private val defaultIndicator = "+"
  private val defVal = choices.find(_.startsWith(defaultIndicator)).map(_.tail)
  private val fixed = choices.map(_.dropWhile(_ == defaultIndicator.head))
  val control = new ComboBox[String](fixed)
  private val defChoice: String = defVal.getOrElse(fixed.head)
  control.selectionModel.value.select(defChoice)
  val valueProperty = new StringProperty()
  valueProperty <==> control.value
}
