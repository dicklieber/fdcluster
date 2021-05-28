package org.wa9nnn.fdcluster.javafx.cluster

import com.wa9nnn.util.DurationFormat
import com.wa9nnn.util.tableui.Cell
import org.scalafx.extras.onFX
import scalafx.scene.control.{Hyperlink, Label, Labeled}
import scalafx.scene.layout.{BorderPane, Pane}

import java.awt.Desktop
import java.net.URI
import java.time.Instant
import java.util.{Timer, TimerTask}
/**
 * One updatable cell in the Cluster Table.
 * The value gets adapted via the [[Cell]] class.
 * The value may already be a Cell which is fine.
 *
 */
//trait PropertyCell(valueName: PropertyCellName, initialValue: Any, classStyles:String*) extends HBox {
trait PropertyCell[T] extends Pane {
  def propertyCellName: PropertyCellName = new PropertyCellName {
    override def toolTip = "???"

    override def name = "???"
  }

  //  styleClass ++= classStyles

  def withStyleClass: PropertyCell[T] = {
    this
  }

  //  update(initialValue)

  def update(value: T): Unit

  def cleanup(): Unit = {
  }

}

case class SimplePropertyCell(override val propertyCellName: PropertyCellName, cssStyleClasses: Seq[String], initialValue: Any) extends BorderPane with PropertyCell[Any] {
  val pane: BorderPane = this
  var maybeStart: Option[Instant] = None
  var maybeTimer: Option[Timer] = None
  var label = new Label()

  styleClass ++= cssStyleClasses

  update(initialValue)

  def update(value: Any): Unit = {
    clear()
    val maybeCell: Option[Cell] = value match {
      case instant: Instant =>
        maybeStart = Some(instant)
        val timer = new Timer("PropertyCellTimer", true)
        maybeTimer = Option(timer)
        styleClass += "ageCell"

        timer.scheduleAtFixedRate(new TimerTask {
          override def run(): Unit = {
            onFX {
              NodeAgeColor(instant, pane)
              label.text = DurationFormat.apply(instant)
              center = label
            }
          }
        }, 0, 750)
        None

      case cell: Cell =>
        Option(cell)
      case x =>
        Option(Cell(x))
    }

    maybeCell.foreach { cell =>
      val control: Labeled = cell.href.map { link =>
        implicit val desktop: Desktop = Desktop.getDesktop
        new Hyperlink(link.url) {
          onAction = _ => {
            desktop.browse(new URI(link.url))
          }
        }
      }.getOrElse(new Label(cell.value))
      cell.tooltip.foreach {
        control.tooltip = _
      }
      styleClass ++= cell.cssClass
      onFX {
        if(cell.cssClass.contains("number"))
          right = control
        else
          left = control
      }
    }
  }

  def clear(): Unit = {
    maybeStart = None
    maybeTimer.foreach(_.cancel)
    maybeTimer = None

  }
}

object SimplePropertyCell {
  def apply(propertyCellName: PropertyCellName, initialValue: Any) = new SimplePropertyCell(propertyCellName, Seq.empty, initialValue)
  def apply( initialValue: Any) = new SimplePropertyCell(PropertyCellName.noName, Seq.empty, initialValue)
  def apply( ) = new SimplePropertyCell(PropertyCellName.noName, Seq.empty, "")
  def css( cssStyleClass:String *) = new SimplePropertyCell(PropertyCellName.noName, cssStyleClass, "")
}

