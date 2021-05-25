package org.wa9nnn.fdcluster.javafx.cluster

import com.wa9nnn.util.DurationFormat
import com.wa9nnn.util.tableui.Cell
import org.scalafx.extras.onFX
import scalafx.css.Styleable
import scalafx.scene.control.{Control, Hyperlink, Label, Labeled}
import scalafx.scene.layout.HBox

import java.awt.Desktop
import java.net.URI
import java.time.Instant
import java.util.{Timer, TimerTask}

/**
 * One updatable cell in the Cluster Table.
 * The value gets adapted via the [[Cell]] class.
 * The value may already be a Cell which is fine.
 *
 * @param initialValue first time, later update via update method.
 */
class PropertyCell(valueName: ValueName, initialValue: Any = "") extends HBox {
  private val pane: Styleable = this
  styleClass += "clusterCell"

  var startAge: Option[Instant] = None
  var maybeTimer: Option[Timer] = None

  update(initialValue)

  def update(value: Any): Unit = {
    cleanup()

    val control: Control = value match {
      case instant: Instant =>
        // want to dislay age, updafed in real-time.
        handleAge(instant)
      case cell: Cell =>
        //Already a Cell
        val control: Labeled = cell.href.map { link =>
          implicit val desktop: Desktop = Desktop.getDesktop
          new Hyperlink(link.url) {
            onAction = event => {
              desktop.browse(new URI(link.url))
            }
          }
        }.getOrElse(new Label(cell.value))
        cell.tooltip.foreach {
          control.tooltip = _
        }
        control.styleClass ++= cell.cssClass
        control

      case _ =>
        // make a Cell use that to populate a Label
        new Label(Cell(value).value)

    }
    control.tooltip =  valueName.getToolTip
    onFX {
      children = Seq(control)
    }
  }

  def cleanup(): Unit = {
    maybeTimer.foreach { t =>
      t.cancel()
      maybeTimer = None
    }
    startAge = None
  }

  def handleAge(instant: Instant): Control = {
    startAge = Option(instant)
    val timer = new Timer("PropertyCellTimer", true)
    val label = new Label()
    timer.scheduleAtFixedRate(new TimerTask {
      override def run(): Unit = {
        val start = startAge.get

        onFX {
          NodeAgeColor(start, pane)
          label.text = DurationFormat.apply(start)
        }
      }
    }, 0, 750)
    maybeTimer = Option(timer)
    label
  }

//  def makeControl(value: Any): Control = {
//    val cell = Cell(value)
//    val control: Labeled = cell.href.map { link =>
//      implicit val desktop: Desktop = Desktop.getDesktop
//      new Hyperlink(link.url) {
//        onAction = event => {
//          desktop.browse(new URI(link.url))
//        }
//      }
//    }.getOrElse(new Label(cell.value))
//    control.tooltip = valueName.getToolTip
//    control
//    //    children = Seq(control)
//  }
}




