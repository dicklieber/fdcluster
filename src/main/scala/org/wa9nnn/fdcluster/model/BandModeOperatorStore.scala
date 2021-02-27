
package org.wa9nnn.fdcluster.model

//import org.wa9nnn.fdcluster.model.BandModeOperator.{Band, Mode}

import org.wa9nnn.fdcluster.model.BandModeOperator.{Band, Mode}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{JsonLogging, Persistence}
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer

import javax.inject.{Inject, Singleton}
@Singleton
class BandModeOperatorStore @Inject()(persistence: Persistence) extends JsonLogging {
  private val bmo: BandModeOperator = persistence.loadFromFile[BandModeOperator]().getOrElse(BandModeOperator())

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
  def bandModeOperator :BandModeOperator = new BandModeOperator(band.value, mode.value, operator.value)

  val knownOperators: ObservableBuffer[CallSign] = ObservableBuffer[CallSign](persistence.loadFromFile[KnownOperators]().getOrElse(new KnownOperators).callSigns)
  knownOperators.onChange { (ov: ObservableBuffer[CallSign], _) =>
    persistence.saveToFile(new KnownOperators(ov.toList))
  }


  private def save(): Unit = {
    val bmo = BandModeOperator(band.value, mode.value, operator.value)
    logJson("bmochange", bmo)
    persistence.saveToFile(bmo)
  }
}

/**
 * safer to construct via {{org.wa9nnn.fdlog.model.BandModeFactory#apply(java.lang.String, java.lang.String)}}
 */
case class BandModeOperator(bandName: Band = "20m", modeName: Mode = "PH", operator: CallSign = "") {
  override def toString: String = s"$bandName"
}

object BandModeOperator {
  type Band = String
  type Mode = String
}

case class KnownOperators(callSigns: List[CallSign] = List.empty)
