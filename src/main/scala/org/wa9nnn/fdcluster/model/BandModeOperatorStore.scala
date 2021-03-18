
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.model

//import org.wa9nnn.fdcluster.model.BandModeOperator.{Band, Mode}

import org.wa9nnn.fdcluster.model.CurrentStation.{Band, Mode}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{Persistence, StructuredLogging}
import scalafx.beans.property.{BooleanProperty, ObjectProperty, ReadOnlyObjectWrapper, StringProperty}
import scalafx.collections.ObservableBuffer

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.{Inject, Singleton}

@Singleton
class BandModeOperatorStore @Inject()(persistence: Persistence) extends StructuredLogging {
  private val bmo: CurrentStation = persistence.loadFromFile[CurrentStation]().getOrElse(CurrentStation())

  val band: StringProperty = new StringProperty(bmo.bandName) {
    onChange { (_, _, _) =>
      save()
    }
  }
  val mode: StringProperty = new StringProperty(bmo.modeName) {
    onChange { (_, _, _) =>
      save()
    }
  }
  val operator: StringProperty = new StringProperty(bmo.operator) {
    onChange { (_, _, _) =>
      save()
    }
  }
  val bandModeOperator: ObjectProperty[CurrentStation] = ReadOnlyObjectWrapper(bmo)
  val bandMode: ReadOnlyObjectWrapper[BandMode] = ReadOnlyObjectWrapper(bmo.bandMode)

  val knownOperators: ObservableBuffer[CallSign] = ObservableBuffer[CallSign](persistence.loadFromFile[KnownOperators]().getOrElse(new KnownOperators).callSigns)
  knownOperators.onChange { (ov: ObservableBuffer[CallSign], _) =>
    persistence.saveToFile(new KnownOperators(ov.toList))
  }


  private def save(): Unit = {
    val bmo = CurrentStation(band.value, mode.value, operator.value)
    logJson("bmochange", bmo)
    bandModeOperator.value = bmo
    bandMode.value = bmo.bandMode
    persistence.saveToFile(bmo)
  }

  if (logger.isTraceEnabled) {
    bandModeOperator.onChange((_, _, nv) =>
      logger.trace(s"bandModeOperator: $bandModeOperator")
    )
    bandMode.onChange((_, _, nv) =>
      logger.trace(s"bandMode: $bandMode")
    )
  }
}

class Compositor(boolProperties: BooleanProperty*) extends BooleanProperty {
  val bools: Array[AtomicBoolean] = Array.fill(boolProperties.size)(new AtomicBoolean())
  boolProperties.zipWithIndex.foreach { case (bp, i) =>
    bp.onChange { (_, _, nv) =>
      bools(i).set(nv)
      calc()
    }
  }

  def calc(): Unit = {
    value = !bools.exists(_.get() == false)
  }
}


/**
 *
 * @param bandName band name limited to whats allow for contest
 * @param modeName CW,DI,PH
 * @param operator callsign of operator. must be a callsign
 * @param rig using this rig free form.
 * @param antenna and this antenna free form.
 */
case class CurrentStation(bandName: Band = "20m", modeName: Mode = "PH", operator: CallSign = "", rig: String = "", antenna: String = "") {
  override def toString: String = s"$bandName $modeName $operator"

  lazy val bandMode: BandMode = BandMode(bandName, modeName)
}

object CurrentStation {
  type Band = String
  type Mode = String

}

case class KnownOperators(callSigns: List[CallSign] = List.empty)

case class BandMode(bandName: Band = "20m", modeName: Mode = "PH") {
  override def toString: CallSign = s"$bandName, $modeName"
}
