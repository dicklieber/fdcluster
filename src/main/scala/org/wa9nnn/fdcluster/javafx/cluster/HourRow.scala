
package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.javafx.cluster.HourRow._
import org.wa9nnn.fdcluster.model.MessageFormats.Digest
import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import org.wa9nnn.fdcluster.store.network.cluster.NodeStateContainer

import scala.collection.immutable


case class HourRow(rowHeader: StyledAny, qhdAndContainers: List[(QsoHourDigest, NodeStateContainer)]) extends Row with LazyLogging {
  private val setOfDigests: Set[Digest] = qhdAndContainers.map {
    _._1.digest
  }.toSet

  val cells: Seq[StyledAny] = {
    if (setOfDigests.size == 1) {
      // all the same
      qhdAndContainers.map(t ⇒ {
        StyledAny(t._1)
          .withCssClass(sameHour)
          .withCssClass(t._2.cssStyles)
      }
      )
    } else {
      // not all the same
      val differentDigests: immutable.Seq[Digest] = setOfDigests.toList.sorted

      qhdAndContainers.map(t ⇒
        StyledAny(t._1)
          .withCssClass(styleForIndex(differentDigests.indexOf(t._1.digest)))
          .withCssClass(t._2.cssStyles)
      )
    }
  }

  logger.debug(s"cells: $cells")

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


