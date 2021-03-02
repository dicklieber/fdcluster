
package org.wa9nnn.util

import java.time.{Duration, Instant}
import org.wa9nnn.util.TimeHelpers._

import com.typesafe.scalalogging.{LazyLogging, Logger}

trait DebugTimer extends LazyLogging {
  def debugTime[A](name: String)(f: => A): A = {
    val start = Instant.now
    try {
      f
    } finally {
      val dur: String = Duration.between(start, Instant.now())
      if (name.contains("$dur")) {
        name.replace("$dur", dur)
      } else {
        logger.debug(s"$name took $dur")
      }
    }
  }
}
