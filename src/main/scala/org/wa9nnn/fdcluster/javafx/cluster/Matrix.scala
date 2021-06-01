package org.wa9nnn.fdcluster.javafx.cluster

import scala.collection.concurrent.TrieMap

/**
 *
 * @tparam R row
 * @tparam C column
 * @tparam T cell what's at an RxC location in the matrix
 */
class Matrix[R <: Ordered[R], C <: Ordered[C], T] {
  private val data = new TrieMap[Key[R, C], T]()

  def size: Int = data.size

  /**
   * @param op  expression that computes the value to store if not already present.
   * @return previous value or None
   */
  def getOrElseUpdate(row: R, column: C, op: => T): T = {
    data.getOrElseUpdate(Key(row, column), op)
  }

  def cellsForRow(row: R): List[T] = {
    data
      .keys
      .filter(_.row == row)
      .map(data(_))
      .toList
  }


  def rows: List[R] = {
    data.keys.foldLeft(Set.empty[R]) { case (set, key) =>
      set + key.row
    }.toList.sorted
  }

  def get(row: R, col: C): Option[T] = {
    data.get(Key(row, col))
  }

  def columns: List[C] = {
    data.keys.foldLeft(Set.empty[C]) { case (set, key) =>
      set + key.column
    }.toList.sorted
  }

  def clear(): Unit = {
    data.clear()
  }

  def removeColumn(column: C): Unit = {
    data.keys.filter(column == _.column).foreach(data.remove)
  }

  def removerow(row: R): Unit = {
    data.keys.filter(row == _.row).foreach(data.remove)
  }
}