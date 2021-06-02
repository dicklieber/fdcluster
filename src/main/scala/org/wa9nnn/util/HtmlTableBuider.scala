package org.wa9nnn.util

import com.wa9nnn.util.tableui.{Cell, Header, Row, Table}

import scala.collection.mutable

class HtmlTableBuider(header: Header) {
  val rowBuilder: mutable.Builder[Row, List[Row]] = List.newBuilder[Row]

  def apply(label: String, value: Any): Unit = {
    val labelCell: Cell = Cell(label)
      .withCssClass("rowHeader")
    rowBuilder += Row(labelCell, value)
  }


  def result: Table = {
    Table(header, rowBuilder.result())
  }
}
