package org.wa9nnn.fdcluster.model

import com.typesafe.config.Config
import com.wa9nnn.util.TimeConverters.instantDisplayUTCLocal
import org.wa9nnn.fdcluster.contest.FieldDaySchedule
import org.wa9nnn.util.{DurationFormat, Message}

import java.net.URI
import java.time.{Duration, Instant}
import scala.util.Try


case class ContestRules(contestName: String,
                        fieldDaySchedule: FieldDaySchedule,
                        categories: EntryCategories,
                        bands: Bands,
                        modes:Modes,
                        uri: Option[URI]
                       ) {

  /**
   * is the suffix of the class valid for this contest?
   *
   * @param designator a part of 1H or 4A.
   * @return
   */
  def validDesignator(designator: String): Boolean = {
    categories.valid(designator)
  }

  val key: String = contestName.filter(_.isUpper)

  def inSchedule(candidate: Instant = Instant.now()): Boolean = fieldDaySchedule.inSchedule(candidate)


  def scheduleMessage: Message = {
    val now = Instant.now()
    if (now.isBefore(fieldDaySchedule.start)) {
      val startsIn = DurationFormat(Duration.between(now, fieldDaySchedule.start))
      Message(s"$contestName starts in $startsIn at ${instantDisplayUTCLocal(fieldDaySchedule.end)}")
        .sad
    } else if (now.isAfter(fieldDaySchedule.end)) {
      val overFor = DurationFormat(Duration.between(fieldDaySchedule.start, now))
      Message(s"$contestName is over, ended at ${instantDisplayUTCLocal(fieldDaySchedule.end)} $overFor ago.")
        .happy
    } else {
      val overIn = DurationFormat(Duration.between(now, fieldDaySchedule.end))
      Message(s"$contestName in: $overIn")
    }
  }
}

object ContestRules {
  def apply(config: Config, defaultConfig: Config): ContestRules = {
    val contestConfig: Config = config.withFallback(defaultConfig)

    new ContestRules(
      contestConfig.getString("contestName"),
      FieldDaySchedule(contestConfig),
      new EntryCategories(contestConfig),
      new Bands(contestConfig),
      new Modes(contestConfig),
      Try(URI.create(contestConfig.getString("contestURL"))).toOption
    )
  }
}
