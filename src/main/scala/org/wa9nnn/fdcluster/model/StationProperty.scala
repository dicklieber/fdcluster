
package org.wa9nnn.fdcluster.model

import _root_.scalafx.beans.property.StringProperty
import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.contest.{OkGate, OkItem}
import org.wa9nnn.fdcluster.javafx.cluster.{NamedValueCollector, NodeValueProvider}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.Station.{Band, Mode}

import java.time.Instant
import javax.inject.{Inject, Singleton}

/**
 * a [[Station]] in an ObjectProperty
 * With convenience properties and current value accessors.
 */
@Singleton
class StationProperty @Inject()(fileContext: FileContext)
  extends PersistableProperty[Station](fileContext) {

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

  //  private val b = Bindings.createObjectBinding[StationTable](
  //    () => {
  //      val r = StationTable(bandNameProperty.value, modeNameProperty.value,
  //        operatorProperty.value,
  //        rigProperty.value, antennaProperty.value)
  //      r
  //    }, bandNameProperty, modeNameProperty, operatorProperty, rigProperty, antennaProperty
  //  )

  //  this <== b

  /**
   * provide a new default instance of T. Needed when there is no file persisted/
   *
   * @return
   */
  override def defaultInstance: Station = {
    logger.debug("Create new empty StationTable")
    Station()
  }

  /**
   * Invoked initially and when the property changes.
   */
  //  override def onChanged(v: StationTable): Unit = {
  //    okToLogProperty.value = v.isOkToOperate
  //  }

  override def isOk: Boolean = value.isOk

  /**
   * Invoked initially and when the property changes.
   */
  override def valueChanged(v: Station): Unit = {


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
                   operator: CallSign = "", rig: String = "", antenna: String = "",
                   stamp: Instant = Instant.now()
                  )
  extends NodeValueProvider with Stamped[Station] with OkContributor{

  def updateOk(): Unit = {
    OkGate (OkItem ("StationTable", "band", "Must specify a band!") {() => bandName.nonEmpty})
    OkGate (OkItem ("StationTable", "mode", "Must specify a band!") {() => modeName.nonEmpty})
    OkGate (OkItem ("StationTable", "operator", "Must specify operator!") {() => modeName.nonEmpty})
  }

  def isOk: Boolean = {
    operator.nonEmpty &&
      bandName.nonEmpty &&
      modeName.nonEmpty
  }

  override def toString: String = s"$bandName $modeName $operator"

  lazy val bandMode: BandMode = BandMode(bandName, modeName)

  override def collectNamedValues(namedValueCollector: NamedValueCollector): Unit = {
    import org.wa9nnn.fdcluster.javafx.cluster.ValueName._

    namedValueCollector(Band, bandName)
    namedValueCollector(Mode, modeName)
    namedValueCollector(Operator, operator)
    namedValueCollector(Rig, rig)
    namedValueCollector(Antenna, antenna)

  }
}

