
package org.wa9nnn.fdcluster.javafx.cluster

import java.time.LocalDateTime

import org.wa9nnn.fdcluster.model.TimeFormat.formatLocalDateTime
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.MessageFormats.Digest
import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import org.wa9nnn.fdcluster.store.network.FdHour
import scalafx.scene.control.TableCell
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.CssClassProvider

class FdClusterTableCell[S, T] extends TableCell[S, T] with LazyLogging {
  item.onChange { (v, oldValue, newValue) =>
    Option(newValue).foreach { v ⇒
      v match {
        case ccp: CssClassProvider ⇒
          styleClass.add(ccp.cssClass)
        case _ ⇒

      }
      try {
        val rendered: String = v match {
          case qhd: QsoHourDigest ⇒
            tooltip =
              s"""Day:\t${qhd.startOfHour.day}
                 |Hour:\t${qhd.startOfHour.hour}
                 |QSOs:\t${qhd.size}
                 |digest:\t${qhd.digest}
                 Digest is a checksum of all the QSO's UUIDs for the hour.""".stripMargin
            val truncated = new String(qhd.digest.take(10))
            s"${qhd.size} $truncated..."
          case fdHour: FdHour ⇒
            tooltip = s"date: ${fdHour.localDate} hour: ${fdHour.hour}"
            fdHour.toString
          case stamp: LocalDateTime ⇒ stamp
          case digest: Digest ⇒
            val truncated = new String(digest.take(10))
            s"$truncated..."
          case digest: DigestValue ⇒
            tooltip = digest.tooltip
            digest.truncated

          case string: String ⇒
            string
          case other ⇒ other.toString
        }
        text = rendered
      } catch {
        case x: Throwable ⇒ x.printStackTrace()
      }

    }

  }
}
