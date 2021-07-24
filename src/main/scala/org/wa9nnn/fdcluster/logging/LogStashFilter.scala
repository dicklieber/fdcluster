package org.wa9nnn.fdcluster.logging


import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class LogStashFilter extends Filter[ILoggingEvent] {
  override def decide(event: ILoggingEvent): FilterReply = {
    try {
      val args = event.getArgumentArray
      if (args == null)
        FilterReply.NEUTRAL
      else {
        val option: Option[AnyRef] = args.find(_.toString == "local=false")
        option.map { localFalse =>
          FilterReply.DENY
        }
          .getOrElse(FilterReply.NEUTRAL)
      }
    } catch {
      case e: Exception =>
        println(s"Filter: ${e.getMessage}")
        FilterReply.NEUTRAL
    }
  }
}
