package org.wa9nnn.fdcluster

import com.typesafe.scalalogging.LazyLogging
import net.logstash.logback.argument.StructuredArguments.kv
import scalafx.beans.property.BooleanProperty

import javax.inject.{Inject, Singleton}
@Singleton
class NetworkControl @Inject()() extends BooleanProperty with LazyLogging {
  value = false
  def isUp: Boolean = value

  onChange { (_, ov, nv) =>
    logger.info(s"Network: {}", kv("up",  nv))
  }

  def up(): Unit = value = true

  def down(): Unit = value = false
}
