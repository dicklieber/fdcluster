
package org.wa9nnn.fdlog.javafx.sync

import java.time.Instant

import scalafx.collections.ObservableBuffer

class StepsData extends ObservableBuffer[Step]{

  def step(name: String, result: String): Unit = {
    +=(Step(name, result))
  }

}


case class Step(name: String, result: String, start: Instant = Instant.now)