
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

package org.wa9nnn.fdcluster.javafx.entry

import _root_.scalafx.geometry.Insets
import _root_.scalafx.scene.control.Label
import _root_.scalafx.scene.layout.{GridPane, Pane}
import _root_.scalafx.scene.text.Text
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.Station.Mode
import org.wa9nnn.fdcluster.model.Qso
import org.wa9nnn.fdcluster.store.AddQsoListener

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

@Singleton
class StatsPane extends AddQsoListener {
  val cw = new Kind("CW")
  val di = new Kind("DI")
  val ph = new Kind("PH")
  var totals = new Totals()
  var nonTotals = Seq(cw, di, ph)
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

  override def add(qso: Qso): Unit = {
    val mode: Mode = qso.bandMode.modeName
    map(mode).increment()

    val tots: (Int, Int) = nonTotals.foldLeft(0, 0) { (accum: (Int, Int), kind: Kind) =>
      (accum._1 + kind.count) -> (accum._2 + kind.points)
    }
    totals.add(tots)
  }

  override def clear(): Unit = {
    totals = new Totals()
    nonTotals = Seq(cw, di, ph)

  }
}

object StatsPane {
  val instanceCounter = new AtomicInteger()
}

trait StatLine {
  val mode: String
  val countCell: Label = new Label("--") {
    styleClass += "statValue"
  }
  val pointsCell: Label = new Label("--") {
    styleClass += "statValue"
  }
  var count = 0
  var points = 0

  def update(): Unit = {
    onFX {
      countCell.text = f"$count%,-6d"
      pointsCell.text = f"$points%,-6d"
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
