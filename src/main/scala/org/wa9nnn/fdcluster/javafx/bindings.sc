import scalafx.beans.Observable
import scalafx.beans.binding.{Bindings, StringBinding}
import scalafx.beans.property.{BooleanProperty, StringProperty}

val a = new StringProperty()
val b: BooleanProperty = Bindings.createBooleanBinding(() =>
  Boolean, dependencies: Observable*
//  () => Option(a.value).getOrElse("").toLowerCase(),
  a
)

a.value = "Hello"
println(s"Setting `a` to ${a.value}, `b` = ${b.value}")