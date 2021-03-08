
package org.wa9nnn.fdcluster

import org.specs2.mutable.Specification
import scalafx.beans.Observable
import scalafx.beans.binding.{Bindings, BooleanBinding, StringBinding}
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.beans.value.ObservableValue

class BindingSpec extends Specification {
  "binding" >> {
    val a = new StringProperty()
    val b = Bindings.createBooleanBinding(() => Option(a.value).getOrElse("") == "Hello", a)



    println(b.value)
    a.value = "Hello"
    println(b.value)


    println(s"Setting `a` to ${a.value}, `b` = ${b.value}")
    ok
  }
}
