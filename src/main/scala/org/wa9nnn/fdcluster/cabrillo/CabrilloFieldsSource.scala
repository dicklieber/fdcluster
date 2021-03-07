
package org.wa9nnn.fdcluster.cabrillo

import com.github.andyglow.config._
import com.typesafe.config.Config
import org.wa9nnn.fdcluster.cabrillo.CabrilloFieldsSource._
import org.wa9nnn.fdcluster.model.BandModeFactory
import org.wa9nnn.util.StructuredLogging
import scalafx.beans.property.StringProperty
import scalafx.scene.control.{ComboBox, Control, Label, TextField, TextArea => fxTextArea}
import scalafx.scene.layout.GridPane

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class CabrilloFieldsSource @Inject()(config: Config,
                                     bandModeFactory: BandModeFactory
                                    )extends StructuredLogging {

  private def parseChoices(choices: String): Seq[String] = {
    choices match
      {
        case "$bands" =>
          bandModeFactory.avalableBands.map(_.band)
        case "$modes" =>
          bandModeFactory.modes.map(_.mode)
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
