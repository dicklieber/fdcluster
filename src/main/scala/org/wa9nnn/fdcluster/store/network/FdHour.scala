
package org.wa9nnn.fdcluster.store.network

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.javafx.cluster.LabelSource
import org.wa9nnn.util.TimeHelpers.{msHour, utcZoneId}
import scalafx.scene.control.Labeled

import java.time.{Instant, ZonedDateTime}
import scala.collection.concurrent.TrieMap

/**
 * Id of a collection of QSOs in a calendar hour.
 * Its just a LocalDateTime with only any hour.
 *
 */
case class FdHour(epochHours: Long) extends Ordered[FdHour] with LabelSource {

  override val (toString, toolTip) = {
    val dt: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochHours / msHour), utcZoneId)
    val day: Int = dt.getDayOfMonth
    val hour: Int = dt.getHour
    (f"$day:$hour%02d", s"utc date: $day hour: $hour")
  }

  /**
   * Used for testing
   */
  def plus(addedHours: Int): FdHour = {
    FdHour(epochHours + addedHours)
  }

  override def compare(that: FdHour): Int = {
    this.epochHours compareTo that.epochHours
  }

  override def setLabel(labeled: Labeled): Unit = {
    labeled.tooltip = toolTip
    labeled.text = toString
  }
}

object FdHour extends LazyLogging {
  /**
   * Used to match any FdHour in [[FdHour.equals()]]
   */
  val allHours: FdHour = FdHour(Long.MinValue)
  val knownHours = new TrieMap[Long, FdHour]()

  def apply(instant: Instant): FdHour = {
    val epochHours = instant.toEpochMilli / msHour
    knownHours.getOrElseUpdate(epochHours, new FdHour(epochHours))
  }
}
