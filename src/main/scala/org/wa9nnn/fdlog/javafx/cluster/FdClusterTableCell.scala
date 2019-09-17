
package org.wa9nnn.fdlog.javafx.cluster

import java.time.LocalDateTime

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.TimeFormat.formatLocalDateTime
import org.wa9nnn.fdlog.model.sync.QsoHourDigest
import scalafx.scene.control.TableCell

class FdClusterTableCell[S, T] extends TableCell[S, T] with LazyLogging {
  item.onChange { (v, j1, j2) =>
    val value = v.value
    text = j2.toString

    try {
      val r = value
      val rendered :String= r match {
        case qhd: QsoHourDigest ⇒
          tooltip = s"${qhd.size} QSOs digest: ${qhd.digest}"
          val truncated = new String(qhd.digest.take(10))
          s"${qhd.size} $truncated..."
        case stamp: LocalDateTime ⇒ stamp
        case string: String ⇒ string
        case other ⇒ other.toString
      }
      text = rendered
    } catch {
      case x: Throwable ⇒ x.printStackTrace()
    }

  }
}
