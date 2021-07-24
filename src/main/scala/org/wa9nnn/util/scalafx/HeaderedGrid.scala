package org.wa9nnn.util.scalafx

import com.wa9nnn.util.tableui.{Cell, Header}
import org.wa9nnn.fdcluster.javafx.cluster.PropertyCell
import scalafx.scene.control.Label
import scalafx.scene.layout.{GridPane, HBox, Pane}

import java.util.concurrent.atomic.AtomicInteger

class HeaderedGrid extends GridPane {
  val rowGenerator: AtomicInteger = new AtomicInteger()

  def header(header: Header): Unit = {
    for {
      headerRow: Seq[Any] <- header.rows
      iRow = rowGenerator.getAndIncrement()
       colGenerator= new AtomicInteger()
      anyVal <- headerRow
      iCol = colGenerator.getAndIncrement()
    } {
      val cell = Cell(anyVal)
      val value: String = cell.value
      val label = new HBox(new Label(value)) {
        styleClass ++= cell.cssClass
        styleClass += "clusterTopRow"
      }
      val colSpan: Int = cell.colSpan
      add(label, iCol, iRow, colSpan, cell.rowSpan)
    }
  }

  def addRow(cells: GridCell*): Unit = {
    val iRow = rowGenerator.getAndIncrement()
    val col = new AtomicInteger
    for {
      gridCell: GridCell <- cells
      (pane, cell) = gridCell.node
    } {
      val iCol = col.getAndIncrement()
      if (iCol == 0)
        pane.styleClass += "clusterRowHeader"
      else
        pane.styleClass += "clusterCell"
      if (cell!=null) {
        add(pane, iCol, iRow, cell.colSpan, cell.rowSpan)
      }else{
        println(s"cell is null!")
      }
    }
  }


}

case class GridCell(c: Either[Cell, PropertyCell]) {
  def node: (Pane, Cell) = {
    val ret = c match {
      case Left(cell) =>
        val container = new HBox(new Label(cell.value)) {
          styleClass ++= cell.cssClass
        }
        container -> cell
      case Right(propertyCell) =>
        propertyCell -> propertyCell.cell

    }

    ret
  }
}

object GridCell {
  def apply(any: Any): GridCell = {
    any match {
      case pc: PropertyCell =>
        new GridCell(Right(pc))
      case a =>
        // Works for anything else include something that's already a Cell.
        GridCell(Left(Cell(a)))
    }
  }
}


