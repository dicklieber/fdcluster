
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

import com.typesafe.config.Config
import org.wa9nnn.util.Message
import scalafx.beans.property.ObjectProperty

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Singleton
class AllContestRules @Inject()(config: Config, contestProperty: ContestProperty)  {

  private val configs: Seq[Config] = config.getObjectList("fdcluster.contests").asScala.map(_.toConfig).toSeq
  private val (defaults, contestConfigs) = configs.partition(_.getString("contestName") == "")
  private val defaultConfig = defaults.head

  val contestNames: Seq[String] = contestConfigs.map(_.getString("contestName")).sorted
  val byContestName: Map[String, ContestRules] = contestConfigs.map(ContestRules(_, defaultConfig)).map(cr => cr.contestName -> cr).toMap

  private def rules(contestName:String):ContestRules = byContestName.getOrElse(contestName, byContestName.head._2)
  contestProperty.onChange{(_,_,nv) =>
    contestRulesProperty.value = rules(nv.contestName)
  }

  val contestRulesProperty: ObjectProperty[ContestRules] =  ObjectProperty[ContestRules](rules(contestProperty.contestName))
  def currentRules:ContestRules = contestRulesProperty.value



  def scheduleMessage: Message = contestRulesProperty.value.scheduleMessage

}

