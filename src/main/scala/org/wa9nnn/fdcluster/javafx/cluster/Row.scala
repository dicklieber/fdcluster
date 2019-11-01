
package org.wa9nnn.fdcluster.javafx.cluster

import org.wa9nnn.fdcluster.javafx.cluster.HourRow._
import org.wa9nnn.fdcluster.model.MessageFormats.Digest
import org.wa9nnn.fdcluster.model.sync.QsoHourDigest

import scala.collection.immutable

trait Row {
  def rowHeader: Any

  def cells: Seq[Any]

}

/**
 *
 * @param rowHeader name show in 1st column of row.
 * @param cells     things that an be rendered.
 */
case class MetadataRow(rowHeader: Any, cells: Seq[Any]) extends Row

case class HourRow(rowHeader: Any, digests: Seq[QsoHourDigest]) extends Row {
  private val set: Set[Digest] = digests.map(_.digest).toSet
  val cells: Seq[DigestValue] = if (set.size == 1) {
    // all the same
    digests.map(DigestValue(_, sameHour))
  } else {
    // not all the same
    val differentDigests: immutable.Seq[Digest] = set.toList.sorted

    digests.map(d ⇒ DigestValue(d, styleForIndex(differentDigests.indexOf(d.digest))))
  }

}

object HourRow {
  val sameHour = "sameHour"
  private val differentStyles = Seq(
    "differentHour1",
    "differentHour2",
    "differentHour3",
    "differentHour4",
    "differentHour5",
    "differentHour6",
    "differentHour7",
    "differentHour8",
  )

  def styleForIndex(index: Int): String = {
    differentStyles(index % 8)
  }
}

