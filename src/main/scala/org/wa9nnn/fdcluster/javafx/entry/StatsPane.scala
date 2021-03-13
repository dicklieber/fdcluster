
package org.wa9nnn.fdcluster.javafx.entry

import com.google.inject.name.Named
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.QsoRecord
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.{GridPane, Pane}
import scalafx.scene.text.Text

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class StatsPane @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord]) extends Pane {

  val cw = new Kind("CW")
  val di = new Kind("DI")
  val ph = new Kind("PH")
  val totals = new Totals()
  private val nonTotals = Seq(cw, di, ph)
  val map: Map[String, Kind] = nonTotals.map(k => k.mode -> k).toMap
  val gridPane: GridPane = new GridPane() {
    hgap = 10
    vgap = 5
    padding = Insets(5, 5, 5, 5)

    val row = new AtomicInteger(1)
    add(new Text("Kind"), 0, 0)
    add(new Text("Count"), 1, 0)
    add(new Text("Points"), 2, 0)

    def add(kind: StatLine): Unit = {
      val r = row.getAndIncrement()
      add(new Label(kind.mode + ":"), 0, r)
      add(kind.countCell, 1, r)
      add(kind.pointsCell, 2, r)
    }

    add(cw)
    add(di)
    add(ph)
    add(totals)
  }
  val pane: Pane = gridPane

  allQsos.onChange { (_, changes) =>
    changes.foreach { change: ObservableBuffer.Change[QsoRecord] =>
      change match {
        case ObservableBuffer.Add(_, added) =>
          added.foreach { qsoRecord =>
            val mode = qsoRecord.qso.bandMode.modeName
            map(mode).increment()

            val tots: (Int, Int) = nonTotals.foldLeft(0, 0) { (accum: (Int, Int), kind: Kind) =>
              (accum._1 + kind.count) -> (accum._2 + kind.points)
            }
            totals.add(tots)
          }
        case ObservableBuffer.Remove(position, removed) =>
        case ObservableBuffer.Reorder(start, end, permutation) =>
        case ObservableBuffer.Update(from, to) =>
      }
    }
  }
}

trait StatLine {
  val mode: String
  val countCell: Label = new Label("--"){
    styleClass += "qsoField"
  }
  val pointsCell: Label = new Label("--"){
    styleClass += "qsoField"
  }
  var count = 0
  var points = 0

  def update(): Unit = {
    onFX {
      countCell.text = f"$count%,6d"
      pointsCell.text = f"$points%,6d"
    }
  }
}

class Kind(val mode: String, val pointsPer: Int = 2) extends StatLine {

  def increment(): Unit = {
    count += 1
    points = count * pointsPer
    update()
  }
}

class Totals(val mode: String = "Total") extends StatLine {

  def add(cp: (Int, Int)): Unit = {
    count = cp._1
    points = cp._2
    update()
  }
}
