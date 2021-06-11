package org.wa9nnn.fdcluster.store

import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.model.Qso
import scalafx.collections.ObservableBuffer

import javax.inject.{Inject, Singleton}

@Singleton
class QsoBuffer @Inject()() extends ObservableBuffer[Qso] with DefaultInstrumented {
  metrics.gauge("QsoBuffer") {
    length
  }

}
