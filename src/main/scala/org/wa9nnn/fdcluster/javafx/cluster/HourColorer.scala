package org.wa9nnn.fdcluster.javafx.cluster

import javafx.collections.ObservableList
import org.wa9nnn.fdcluster.javafx.cluster.HourColorer.prefix
import org.wa9nnn.fdcluster.model.sync.{QsoDigestPropertyCell, QsoHourDigest}
import scalafx.collections.ObservableBuffer
import scalafx.css.Styleable

import scala.collection.immutable

object HourColorer {
  val prefix = "fdh"
  val same = "fdhSameHour"
  val diffs: List[String] = List.tabulate(7)(n => s"fdhDiff$n")
  private val colors: List[String] = same +: diffs


  def apply(cells: Seq[QsoDigestPropertyCell]): Unit = {
    if (cells.tail.forall(_.current == cells.head.current)) {
      // all the same
      cells.foreach(cell =>
        colorCell(cell, same))
    } else {
      // not all the same
      val head = cells.head
      val values = cells.groupBy(_.current)
      val groups: List[(Seq[QsoDigestPropertyCell], Int)] = values.values
        .toList
        .sortBy(_.size)
        .zipWithIndex

      for {
        cellsWithIndex <- groups
        classStyle = colors(cellsWithIndex._2)
        cell <- cellsWithIndex._1
      } {
        colorCell(cell, classStyle)
      }
    }
  }

  val ff: (Styleable, String) => Unit = StylableHelper(colors)

  def colorCell(qsoDigestPropertyCell: QsoDigestPropertyCell, classStyle: String): Unit = {
    ff(qsoDigestPropertyCell, classStyle)
  }
}

object StylableHelper {
  /**
   *
   * @param toremove style to remove.
   * @param styleable to be updated.
   * @param styleClass new classStyle
   */
  def apply(toremove:List[String])(styleable: Styleable, styleClass:String): Unit = {
    val clazz = styleable.styleClass
    clazz.removeAll(toremove:_*)
    clazz += styleClass
  }
}
