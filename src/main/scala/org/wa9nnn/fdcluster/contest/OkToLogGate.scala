package org.wa9nnn.fdcluster.contest

import com.wa9nnn.util.tableui.{Cell, Header}
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.javafx.cluster.{NamedValue, PropertyCell, PropertyCellName}
import org.wa9nnn.util.scalafx.HappySad._
import org.wa9nnn.util.scalafx.{GridCell, HeaderedGrid}
import scalafx.beans.property.BooleanProperty
import scalafx.scene.control.Label
import scalafx.scene.layout.{GridPane, HBox}

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap

/**
 * Keeps track of if its ok to log QSOs.
 * All run-time interaction should go through the companion class.
 */
protected class OkGate {
  protected val map = new TrieMap[OkItemKey, OkItemPropertyCell]

  def update(okItem: OkItem): Unit = {
    map.getOrElseUpdate(okItem.okItemKey, OkItemPropertyCell(okItem)).update(okItem)
  }

  /**
   *
   * @return empty if ok, otherwise missing stuff
   */
  def getBad: List[OkItemPropertyCell] = {
    map.values.filterNot(_.isOk).toList.sorted
  }

  def getAll: List[OkItemPropertyCell] = map.values.toList.sorted
}

object OkGate extends BooleanProperty {
  private val gate = new OkGate

  def apply(okItem: OkItem): Unit = {
    gate.update(okItem)
    value = checkOk.isEmpty
  }

  def checkOk: List[OkItemPropertyCell] = gate.getBad

  def allOkItems: List[OkItemPropertyCell] = gate.getAll

  def pane: GridPane = {
    val headeredGrid = new HeaderedGrid
    headeredGrid.header(Header( "Ok to Log Gate", "Locus", "Item", "Condition"))

    allOkItems.foreach { okItemPropertyCell =>
      val okItem = okItemPropertyCell.value
      headeredGrid.addRow(
        Seq(
          GridCell(okItem.okItemKey.locus),
          GridCell(okItem.okItemKey.itemName),
          GridCell(okItemPropertyCell)
        ): _*
      )
    }
    headeredGrid
  }
}

/**
 *
 * @param okItemKey
 * @param failReason whats bad.
 */
case class OkItem(okItemKey: OkItemKey, failReason: Option[String]) extends Ordered[OkItem] {
  val isOK: Boolean = failReason.isEmpty

  override def compare(that: OkItem): Int = this.okItemKey compareTo that.okItemKey
}

/**
 * @param locus          area where the value can be fixed.
 * @param itemName       within the locus
 */
case class OkItemKey(locus: String, itemName: String) extends Ordered[OkItemKey] with PropertyCellName {
  override def compare(that: OkItemKey): Int = {
    locus.compareToIgnoreCase(that.locus) match {
      case 0 =>
        itemName compareToIgnoreCase that.itemName
      case r =>
        r
    }
  }

  override def toolTip: String = ""

  override def name: String = s"$locus:$itemName"
}

case class OkItemPropertyCell(initialValue: OkItem) extends PropertyCell with Ordered[OkItemPropertyCell] {
  val label = new Label()
  val container: HBox = new HBox(label) {
    styleClass += "clusterCell"
  }
  left = container
  var value: OkItem = initialValue

  def isOk: Boolean = value.isOK

  def update(okItem: OkItem): Unit = {
    value = okItem
    val maybeReason = okItem.failReason

    onFX {
      maybeReason match {
        case Some(reason) =>
          cell = Cell(reason).withCssClass("sad")
          sad(label, reason)
        case None =>
          val ok = "Ok"
          cell = Cell(ok).withCssClass("happy")
          happy(label, ok)
      }
    }
  }

  override def update(namedValue: NamedValue): Unit = {
    throw new IllegalArgumentException("Use ")
  }

  override def compare(that: OkItemPropertyCell): Int = this.value.okItemKey compareTo (that.value.okItemKey)
}

object OkItem {
  /**
   *
   * @param okItemKey      id.
   * @param failReason     used if test fails.
   * @param test           if false then failReason becomes Option[failedReason]
   * @return
   */
  def apply(okItemKey: OkItemKey, failReason: String)(test: () => Boolean): OkItem = {
    new OkItem(okItemKey, if (!test())
      Option(failReason)
    else
      None
    )
  }

  def apply(locus: String, itemName: String, failReason: String)(test: () => Boolean): OkItem = {
    apply(OkItemKey(locus, itemName), failReason: String)(test)
  }
}


