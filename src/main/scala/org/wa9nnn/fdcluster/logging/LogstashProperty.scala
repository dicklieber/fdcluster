package org.wa9nnn.fdcluster.logging

import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{PersistableProperty, Stamped}
import org.wa9nnn.util.HostPort

import java.time.Instant
import javax.inject.{Inject, Singleton}
@Singleton
class LogstashProperty @Inject()(fileContext: FileContext, logManager: LogManager) extends PersistableProperty[EnabledDestination](fileContext){
  /**
   * provide a new default instance of T. Needed when there is no file persisted/
   *
   * @return
   */
  override def defaultInstance: EnabledDestination = EnabledDestination()

  override def isOk: Boolean = true

  /**
   * Invoked initially and when the property changes.
   */
  override def valueChanged(v: EnabledDestination): Unit = {
    logManager.logstash(v)
  }
}

case class EnabledDestination(hostPort:HostPort = HostPort("127.0.0.1", 5044), enabled:Boolean = false, stamp:Instant = Instant.now()) extends Stamped[EnabledDestination]