
package org.wa9nnn.fdlog.javafx.sync

import java.time.Instant

import scalafx.collections.ObservableBuffer

import scala.language.implicitConversions


class StepsDataMethod (val ob:ObservableBuffer[Step]) {
  def step(name: String, result: Any): Unit = {
     val s = result match {
      case s:String ⇒ s
      case i:Int ⇒ f"$i%,d"
      case x ⇒ x.toString
    }
    val step = Step(name, s)
    ob += step
  }

}

object StepsDataMethod {
  implicit def addStep(ob: ObservableBuffer[Step]) = new StepsDataMethod(ob)
}

case class Step(name: String, result: String, start: Instant = Instant.now)