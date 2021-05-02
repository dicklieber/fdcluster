
package org.wa9nnn.fdcluster.model

import _root_.scalafx.beans.binding.Bindings
import _root_.scalafx.beans.property.{ObjectProperty, StringProperty}
import org.wa9nnn.fdcluster.model.CurrentStation.{Band, Mode}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence

import javax.inject.{Inject, Singleton}

/**
 * a [[CurrentStation]] in an ObjectProperty
 * With convenience properties and current value accessors.
 */
@Singleton
class CurrentStationProperty @Inject()(persistence: Persistence)
  extends ObjectProperty[CurrentStation](
    null,
    "CurrentStation",
    persistence.loadFromFile[CurrentStation](() => CurrentStation())) {

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

  val b = Bindings.createObjectBinding[CurrentStation](
    () => {
      val r = CurrentStation(bandNameProperty.value, modeNameProperty.value,
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

object CurrentStation {
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
case class CurrentStation(bandName: Band = "20m", modeName: Mode = "PH",
                          operator: CallSign = "", rig: String = "", antenna: String = "") {
  override def toString: String = s"$bandName $modeName $operator"

  lazy val bandMode: BandMode = BandMode(bandName, modeName)
}


