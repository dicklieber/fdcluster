package org.wa9nnn.fdcluster.javafx.cluster

import org.specs2.mutable.Specification

case class Row(i: Int) extends Ordered[Row] {
  override def compare(that: Row): Int = this.i compareTo (that.i)
}

case class Column(ch: Char) extends Ordered[Column] {
  override def compare(that: Column): Int = this.ch compareTo (that.ch)
}

case class TCell(row: Row, column: Column)

class MatrixSpec extends Specification {
  val columnC: Column = Column('c')

  def m3(): Matrix[Row, Column, TCell] = {
    val matrix3 = new Matrix[Row, Column, TCell]

    def addCell(row: Row, column: Column): Unit = {
      val tc = TCell(row, column)
      matrix3.getOrElseUpdate(tc.row, tc.column, tc)
    }

    addCell(row1, columnA)
    addCell(row1, columnC)
    addCell(row2, columnC)
    addCell(row2, columnD)

    matrix3
  }

  "MatrixSpec" >> {
    "initial" >> {
      val matrix = new Matrix[Row, Column, TCell]
      matrix.size must beEqualTo(0)
    }
  }

  private val row1: Row = Row(1)
  private val row2: Row = Row(2)
  private val columnA: Column = Column('a')
  private val columnD: Column = Column('d')
  private val columnE: Column = Column('e')

  "inserts" >> {
    val matrix = new Matrix[Row, Column, Long]

    val value: Long = matrix.getOrElseUpdate(row1, columnA, 42)
    value must beEqualTo(42)

    val l: Long = matrix.getOrElseUpdate(row1, columnA, 10)
    l must beEqualTo(42) // unchanged as it existed.
  }


  "happy path" >> {
    val matrix3 = m3()
    val rows = matrix3.cellsForRow(row1)
    rows must haveLength(2)
    matrix3.get(row1, columnA) must beSome(TCell(row1, columnA))
    matrix3.get(row2, columnE) must beNone
  }

  "clear" >> {
    val matrix3 = m3()
    matrix3.size must beEqualTo(4)
    matrix3.clear()
    matrix3.size must beEqualTo(0)
  }


  "columns" >> {
    val matrix3 = m3()
    matrix3.columns must haveLength(3)
  }


  "rows" >> {
    val matrix3 = m3()
    matrix3.rows must haveLength(2)
  }

  "remove column" >> {
    val matrix3 = m3()
    matrix3.removeColumn(columnA)
    matrix3.columns must haveLength(2)
    matrix3.size must beEqualTo(3)
  }
  "remove row" >> {
    val matrix3 = m3()
    matrix3.removerow(row1)
    matrix3.rows must haveLength(1)
    matrix3.size must beEqualTo(2)
  }
}
