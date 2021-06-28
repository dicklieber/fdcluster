package org.wa9nnn.fdcluster.store.network.testapp

import org.specs2.mutable.Specification

class FixedObservableBufferSpec extends Specification {

  "FixedObservableBuffer" should {
    "prepend" in {
      val buffer = new FixedObservableBuffer[String](2)
      buffer must haveLength(0)
      buffer.prepend("one")
      buffer must haveLength(1)
      buffer.prepend("two")
      buffer must haveLength(2)
      buffer.prepend("three")
      buffer must haveLength(2)
    }
  }
}
