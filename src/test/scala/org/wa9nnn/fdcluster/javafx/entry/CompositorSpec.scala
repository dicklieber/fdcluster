package org.wa9nnn.fdcluster.javafx.entry

import org.specs2.mutable.Specification
import scalafx.beans.property.BooleanProperty

class CompositorSpec extends Specification {
  sequential
  var state = false
  "CompositorSpec" should {
    val p1 = new BooleanProperty()
    val p2 = new BooleanProperty()
    val p3 = new BooleanProperty()
    val compositor = new Compositor(p1, p2, p3)
    compositor.onChange { (_, _, nv) =>
      state = nv
    }
    p1.value = false;
    state must beEqualTo(false)
    p1.value = true;
    state must beEqualTo(false)
    p2.value = true;
    state must beEqualTo(false)
    p3.value = true
    state must beEqualTo(true)

    "calc" in {
      ok
    }
  }
}
