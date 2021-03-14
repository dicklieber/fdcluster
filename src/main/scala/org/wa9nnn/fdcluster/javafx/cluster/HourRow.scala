
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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


