
package org.wa9nnn.fdcluster.javafx.entry

import scalafx.beans.property.BooleanProperty

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Listens to a bunch of [[BooleanProperty]]s and updates the state booleanProperty
 *
 * @param boolProperties to be watched.
 */
class Compositor(boolProperties: BooleanProperty*) extends BooleanProperty {
  val bools: Array[AtomicBoolean] = Array.fill(boolProperties.size)(new AtomicBoolean())
  boolProperties.zipWithIndex.foreach { case (bp, i) =>
    bp.onChange { (_, _, nv) =>
      bools(i).set(nv)
      calc()
    }
  }

  def calc(): Unit = {
    value = !bools.exists(_.get()==false)
  }
}
