
package org.wa9nnn.fdlog.javafx.cluster

import java.time.LocalDateTime

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.MessageFormats.Digest
import org.wa9nnn.fdlog.model.TimeFormat.formatLocalDateTime
import org.wa9nnn.fdlog.model.sync.QsoHourDigest
import org.wa9nnn.fdlog.store.network.FdHour
import scalafx.scene.control.TableCell

class FdClusterTableCell[S, T] extends TableCell[S, T] with LazyLogging {
  item.onChange { (v, oldValue, newValue) =>
    Option(newValue).foreach{v ⇒

      try {
        val rendered :String= v match {
          case qhd: QsoHourDigest ⇒
            tooltip =
              s"""Day:\t${qhd.startOfHour.day}
                 |Hour:\t${qhd.startOfHour.hour}
                 |QSOs:\t${qhd.size}
                 |digest:\t${qhd.digest}
                 Digest is a checksum of all the QSO's UUIDs for the hour.""".stripMargin
            val truncated = new String(qhd.digest.take(10))
            s"${qhd.size} $truncated..."
          case fdHour:FdHour ⇒
            tooltip = s"Day: ${fdHour.localDate} hour: ${fdHour.hour}"
            fdHour.toString
          case stamp: LocalDateTime ⇒ stamp
          case digest:Digest ⇒
            val truncated = new String(digest.take(10))
            s"$truncated..."
          case string: String ⇒ string
          case other ⇒ other.toString
        }
        text = rendered
      } catch {
        case x: Throwable ⇒ x.printStackTrace()
      }

    }

  }
}
