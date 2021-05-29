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

  def foreachCol(col: C, f: T => Unit): Unit =
    data.keys.filter(_.column == col).foreach { k =>
      f(data(k))
    }

  /**
   *
   * @param key with R & C.
   * @param op  expression that computes the value to store if not already present.
   * @return previous value or None
   */
  def getOrElseUpdate(key: Key[R, C], op: => T): T = {
    data.getOrElseUpdate(key, op)
  }

  def cellForRow(row: R): List[T] = {
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

}