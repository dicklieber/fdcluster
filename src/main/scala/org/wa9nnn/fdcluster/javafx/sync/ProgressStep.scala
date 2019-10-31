
package org.wa9nnn.fdcluster.javafx.sync

import java.time.Instant

import scalafx.collections.ObservableBuffer

import scala.language.implicitConversions


class StepsDataMethod (val ob:ObservableBuffer[ProgressStep]) {
  def step(name: String, result: Any): Unit = {
     val s = result match {
      case s:String ⇒ s
      case i:Int ⇒ f"$i%,d"
      case x ⇒ x.toString
    }
    val step = ProgressStep(name, s)
    ob += step
  }

}

object StepsDataMethod {
  implicit def addStep(ob: ObservableBuffer[ProgressStep]) = new StepsDataMethod(ob)
}

case class ProgressStep(name: String, result: String, start: Instant = Instant.now)