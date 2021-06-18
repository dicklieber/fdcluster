package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.DurationFormat
import com.wa9nnn.util.tableui.Cell
import org.scalafx.extras.onFX
import scalafx.scene.control.{Hyperlink, Label}
import scalafx.scene.layout.BorderPane

import java.awt.Desktop
import java.net.URI
import java.time.Instant
import java.util.{Timer, TimerTask}

object PropertyCellFactory {
  def apply(name: PropertyCellName, value: Any): PropertyCell = {
    apply(NamedValue(name, value))
  }

  def apply(namedValue: NamedValue): PropertyCell = {
    namedValue match {
      case nv@NamedValue(ValueName.Age, _) =>
        PropertyCellAge(nv)
      case nv@NamedValue(_, v) =>
        v match {
          case cell: Cell if cell.href.isDefined =>
            LinkPropertyCell(nv)
          case _ =>
            TextPropertyCell(nv)
        }
    }
  }
}

/**
 * One updatable cell in the Cluster Table.
 * The value gets adapted via the [[Cell]] class.
 * The value may already be a Cell which is fine.
 *
 */
trait PropertyCell extends BorderPane {

  def update(namedValue: NamedValue): Unit

  def cleanup(): Unit = {
  }
  var cell: Cell = _

}

case class TextPropertyCell(namedValue: NamedValue)
  extends PropertyCell with LazyLogging {

  val label: Label = new Label
  left = label
  //to handle number style to right.
  styleClass += "clusterCell"

  update(namedValue)

  def update(namedValue: NamedValue): Unit = {
    cell = Cell(namedValue.value)
    onFX {
      cell.tooltip.foreach {
        label.tooltip = _
      }
      try {
        styleClass.removeAll(cell.cssClass: _*)
        styleClass.addAll(cell.cssClass)
      } catch {
        case e: Exception =>
          logger.error(s"Applying ${cell.cssClass} to $styleClass", e)
      }
      label.text = cell.value
    }
  }

}

case class LinkPropertyCell(namedValue: NamedValue)
  extends PropertyCell with LazyLogging {
  implicit val desktop: Desktop = Desktop.getDesktop

  var hyperLink: Hyperlink = new Hyperlink(namedValue.value.asInstanceOf[Cell].value)
  left = hyperLink

  styleClass += "clusterCell"

  update(namedValue)

  def update(namedValue: NamedValue): Unit = {
     cell = Cell(namedValue.value)
    if (cell.href.isEmpty)
      throw new IllegalArgumentException(s"namedValue must be cell with href!")
    onFX {
      hyperLink.text = cell.value
      hyperLink.onAction = _ => {
        desktop.browse(new URI(cell.href.get.url))
      }
    }
  }
}

case class PropertyCellAge(namedValue: NamedValue) extends PropertyCell {
  assert(namedValue.name == ValueName.Age, "Must be an Age")
  val label: Label = new Label()
  left = label
  var currentInstant: Instant = Instant.now()

  private var maybeTimer: Option[Timer] = None
  private val timer = new Timer("PropertyCellTimer", true)
  styleClass += "ageCell"
  timer.scheduleAtFixedRate(new TimerTask {
    override def run(): Unit = {
      onFX {
        NodeAgeColor(currentInstant, label)
        label.text = DurationFormat.apply(currentInstant)
      }
    }
  }, 5, 750)

  override def update(namedValue: NamedValue): Unit = {
    currentInstant = namedValue.value.asInstanceOf[Instant]
  }

  def clear(): Unit = {
    maybeTimer.foreach(_.cancel)
    maybeTimer = None
  }

}

//object TextPropertyCell {
//  def apply(propertyCellName: PropertyCellName, initialValue: Any) = new TextPropertyCell(propertyCellName, Seq.empty, initialValue)
//
//  def apply(initialValue: Any) = new TextPropertyCell(PropertyCellName.noName, Seq.empty, initialValue)
//
//  def apply() = new TextPropertyCell(PropertyCellName.noName, Seq.empty, "")
//
//  def css(cssStyleClass: String*) = new TextPropertyCell(PropertyCellName.noName, cssStyleClass, "")
//}

