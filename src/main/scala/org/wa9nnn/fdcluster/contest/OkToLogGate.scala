package org.wa9nnn.fdcluster.contest

import com.wa9nnn.util.tableui.{Cell, Row}
import scalafx.beans.property.BooleanProperty

import scala.collection.concurrent.TrieMap

/**
 * Keeps track of if its ok to log QSOs.
 * All run-time interaction should go through the companion class.
 */
protected class OkGate {
  private val map = new TrieMap[String, OkItem]()

  def update(okItem: OkItem): Unit = {
    map.put(okItem.key, okItem)
  }

  /**
   *
   * @return empty if ok, otherwise missing stuff
   */
  def getBad: List[OkItem] = {
    map.values.filterNot(_.isOK).toList.sorted
  }

  def getAll: List[OkItem] = map.values.toList.sorted

}

object OkGate extends BooleanProperty {
  private val gate = new OkGate

  def apply(okItem: OkItem): Unit = {
    gate.update(okItem)
    value = checkOk.isEmpty
  }

  def checkOk: List[OkItem] = gate.getBad
  def allOkItems: List[OkItem] = gate.getAll
}

/**
 *
 * @param locus      area where the value can be fixed.
 * @param name       within the locus
 * @param failReason whats bad.
 */
case class OkItem(locus: String, name: String, failReason: Option[String]) extends Ordered[OkItem] {
  val isOK: Boolean = failReason.isEmpty
  val key: String = locus + ":" + name

  override def compare(that: OkItem): Int = this.name compareToIgnoreCase that.name

  def row(): Row = {
    Row(locus, name, failReason
      .map(reason => Cell(reason)
        .withCssClass("sad"))
      .getOrElse(Cell("Ok")
        .withCssClass("happy")))
  }
}

object OkItem {
  /**
   *
   * @param locus      area.
   * @param name       item within area.
   * @param failReason used if test fails.
   * @param test       if false then failReason becomes Option[failedReason]
   * @return
   */
  def apply(locus: String, name: String, failReason: String)( test: () => Boolean): OkItem = {
    OkItem(locus, name, if (test())
      Option(failReason)
    else
      None
    )
  }
}


