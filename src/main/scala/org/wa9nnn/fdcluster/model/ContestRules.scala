
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

package org.wa9nnn.fdcluster.model

import com.typesafe.config.{Config, ConfigFactory}
import com.wa9nnn.util.TimeConverters.instantDisplayUTCLocal
import org.wa9nnn.fdcluster.contest.FieldDaySchedule
import org.wa9nnn.util.{DurationFormat, Message}
import _root_.scalafx.beans.property.{ObjectProperty, StringProperty}

import java.time.{Duration, Instant}
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Singleton
class AllContestRules @Inject()(config: Config, contestProperty: ContestProperty) extends ObjectProperty[ContestRules] {

  val contestNames: Seq[String] = config.getStringList("contest.contestNames").asScala.toList
  val byContestName: Map[String, ContestRules] = contestNames.map(ContestRules(_, config)).map(cr => cr.contestName -> cr).toMap

  private val event: StringProperty = contestProperty.contestNameProperty
  val contestRulesProperty: ObjectProperty[ContestRules] = ObjectProperty[ContestRules](byContestName(event.value))

  contestProperty.contestNameProperty.onChange { (_, _, nv) =>
    contestRulesProperty.value = byContestName(nv)
  }

  def scheduleMessage: Message = contestRulesProperty.value.scheduleMessage
}


/**
 * fixed (i.e. from application.conf)
 */
case class ContestRules(contestName: String, appConfig: Config) {
  def validDesignator(designator: String): Boolean = {
    categories.valid(designator)
  }

  private val configPath = s"/contests/$contestName.conf"
  val contestConfig: Config = ConfigFactory.parseURL(getClass.getResource(configPath)).withFallback(appConfig).getConfig("contest")

  private val fieldDaySchedule: FieldDaySchedule = FieldDaySchedule(contestConfig)

  def inSchedule(candidate: Instant): Boolean = fieldDaySchedule.inSchedule(candidate)

  val categories: EntryCategories = new EntryCategories(contestConfig)

  def scheduleMessage: Message = {
    val now = Instant.now()
    if (now.isBefore(fieldDaySchedule.start)) {
      val startsIn = DurationFormat(Duration.between(now, fieldDaySchedule.start ))
     Message( s"Contest starts in $startsIn at ${instantDisplayUTCLocal(fieldDaySchedule.end)}")
       .sad
    } else if(now.isAfter(fieldDaySchedule.end)){
      val overFor = DurationFormat(Duration.between(fieldDaySchedule.start, now))
      Message(s"Contest is over, ended at ${instantDisplayUTCLocal(fieldDaySchedule.end)} $overFor ago.")
        .happy
    }else{
      val overIn = DurationFormat(Duration.between(now, fieldDaySchedule.end))
      Message(s"Over in: $overIn")

    }
  }

}
