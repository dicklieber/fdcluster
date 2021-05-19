package org.wa9nnn.fdcluster

import com.typesafe.scalalogging.LazyLogging
import scalafx.beans.property.BooleanProperty

import java.lang
import javax.inject.{Inject, Singleton}

@Singleton
class NetworkControl @Inject()() extends BooleanProperty with LazyLogging {
  value = false
  def isUp: Boolean = value

  onChange { (_, ov, nv) =>
    logger.info(s"Network up: $nv")
  }

  def up(): Unit = value = true

  def down(): Unit = value = false
}
