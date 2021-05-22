
package org.wa9nnn.fdcluster.model

import _root_.scalafx.beans.binding.Bindings
import _root_.scalafx.beans.property.{ObjectProperty, StringProperty}
import org.wa9nnn.fdcluster.javafx.NamedCellProvider
import org.wa9nnn.fdcluster.model.Station.{Band, Mode}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence

import javax.inject.{Inject, Singleton}

/**
 * a [[Station]] in an ObjectProperty
 * With convenience properties and current value accessors.
 */
@Singleton
class StationProperty @Inject()(persistence: Persistence)
  extends ObjectProperty[Station](
    null,
    "Station",
    persistence.loadFromFile[Station](() => Station())) {

  private val currentVals = value

  def bandName: Band = currentVals.bandName

  val bandNameProperty: StringProperty = StringProperty(bandName)

  def modeName: Mode = currentVals.modeName

  val modeNameProperty: StringProperty = StringProperty(modeName)

  def operator: CallSign = currentVals.operator

  val operatorProperty: StringProperty = StringProperty(operator)

  def rig: String = currentVals.rig

  val rigProperty: StringProperty = StringProperty(rig)

  def antenna: String = currentVals.antenna

  val antennaProperty: StringProperty = StringProperty(antenna)

  def bandMode: BandMode = BandMode(bandName, modeName)

  val b = Bindings.createObjectBinding[Station](
    () => {
      val r = Station(bandNameProperty.value, modeNameProperty.value,
        operatorProperty.value,
        rigProperty.value, antennaProperty.value)
      r
    }, bandNameProperty, modeNameProperty, operatorProperty, rigProperty, antennaProperty
  )

  this <== b

  onChange { (_, _, to) =>
    persistence.saveToFile(to)
  }
}

object Station {
  type Band = String
  type Mode = String


}

/**
 *
 * @param bandName band name limited to whats allow for contest
 * @param modeName CW,DI,PH
 * @param operator callSign of operator. must be a callSign
 * @param rig      using this rig free form.
 * @param antenna  and this antenna free form.
 */
case class Station(bandName: Band = "20m", modeName: Mode = "PH",
                   operator: CallSign = "", rig: String = "", antenna: String = "")
  extends NamedCellProvider[Station] {
  override def toString: String = s"$bandName $modeName $operator"

  lazy val bandMode: BandMode = BandMode(bandName, modeName)
}

