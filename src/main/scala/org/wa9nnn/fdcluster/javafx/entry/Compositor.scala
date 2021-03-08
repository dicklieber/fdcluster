
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
    println(value)
  }


}

object CompositorTest extends App {
  private val p1 = new BooleanProperty()
  private val p2 = new BooleanProperty()
  private val p3 = new BooleanProperty()
  val compositor = new Compositor(p1, p2, p3)
  compositor.onChange{(_,_,nv) =>
   println(nv)
  }
  p1.value = false
  p1.value = true
  p2.value = true
  p3.value = true
}