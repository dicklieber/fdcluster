package org.wa9nnn.fdcluster.javafx

import com.wa9nnn.util.tableui.Cell
import org.wa9nnn.fdcluster.javafx.cluster.{NamedValue, ValueName}
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import scalafx.scene.control.Label

import scala.collection.mutable


object NamedCellProvider {
  val omitFieldNames = Set("stamp", "v")
}


/**
 * A Label that can be updated
 *
 * @param name         of field.
 * @param value        starting.
 */
case class CellProperty(namedValue: NamedValue) extends Label{
  text = Cell(namedValue.value).value

  def value(newVal: Any): Unit = {
    text = Cell(newVal).value
  }
}


case class ValuesForNode(nodeAddress: NodeAddress, qsoHourDigests:List[QsoHourDigest])  {
  private val parameters: mutable.Builder[NamedValue, List[NamedValue]] = List.newBuilder[NamedValue]

  def apply(name: ValueName, value: Any): Unit = {
    parameters += NamedValue( name, value)
  }

  def result: List[NamedValue] = parameters.result()
}

