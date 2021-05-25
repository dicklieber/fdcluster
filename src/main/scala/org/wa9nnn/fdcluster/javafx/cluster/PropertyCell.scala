package org.wa9nnn.fdcluster.javafx.cluster

import com.wa9nnn.util.DurationFormat
import com.wa9nnn.util.tableui.Cell
import org.scalafx.extras.onFX
import scalafx.css.Styleable
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

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
  private val label = new Label()
  private val pane:Styleable = this
  label.tooltip = valueName.getToolTip

  children = Seq(label)
  styleClass += "clusterCell"
  update(initialValue)
  var startAge: Option[Instant] = None

  def update(value: Any): Unit = {
    value match {
      case instant: Instant =>
        startAge = Option(instant)
        new Timer(true).scheduleAtFixedRate(new TimerTask {
          override def run(): Unit = {
            val start = startAge.get

            onFX {
              NodeAgeColor(start, pane)
              label.text = DurationFormat.apply(start)
            }
          }
        }, 250, 750)
      case _ =>
    }
    if (value.isInstanceOf[Instant]) {
    }

    onFX {
      label.text = Cell(value).value
    }
  }

  override def toString(): String = {
    s"PropertyCell: $valueName: ${label.text.value}"
  }

  def cleanup(): Unit = {

  }
}


