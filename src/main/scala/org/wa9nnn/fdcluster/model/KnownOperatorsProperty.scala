
package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence
import scalafx.beans.property.ObjectProperty

import javax.inject.{Inject, Singleton}

@Singleton
class KnownOperatorsProperty @Inject()(persistence: Persistence)
  extends ObjectProperty[KnownOperators]() {
  def add(newOperator: String): Unit = {
    value = value.add(newOperator)
  }

  value = persistence.loadFromFile[KnownOperators](() => KnownOperators())

  onChange { (_, _, nv) =>
    persistence.saveToFile(value)
  }

}

case class KnownOperators(callSigns: Seq[CallSign] = Seq.empty) {
  def add(callSign: CallSign): KnownOperators = {
    KnownOperators((callSign +: callSigns).distinct.sorted)
  }
}
