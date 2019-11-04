
package org.wa9nnn.fdcluster.javafx.cluster

import java.time.LocalDateTime

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.TimeFormat.formatLocalDateTime
import scalafx.scene.control.TableCell

class FdClusterTableCell[S, T] extends TableCell[Row, T] with LazyLogging {
  item.onChange { (_, _, newValue) =>
    Option(newValue).foreach { v: T ⇒
      try {
        v match {
          case sa: StyledAny ⇒
            sa.setLabel(this)
          case stamp: LocalDateTime ⇒
            text = stamp
          case string: String ⇒
            text = string
          case other ⇒
            text = other.toString
        }
      } catch {
        case x: Throwable ⇒ x.printStackTrace()
      }
//      val styles = styleClass.toList
//      logger.debug(s"value: $v styles: $styles")
    }

  }
}
