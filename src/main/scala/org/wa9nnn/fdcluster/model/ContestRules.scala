
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

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

/**
 * fixed (i.e. from application.conf)
 */
case class ContestRules(contestName: String, appConfig: Config) {
  def validDesignator(designator: String): Boolean = {
    categories.valid(designator)

  }

  private val configPath = s"/contests/$contestName.conf"
  val contestConfig: Config = ConfigFactory.parseURL(getClass.getResource(configPath)).withFallback(appConfig).getConfig("contest")

  val categories: EntryCategories = new EntryCategories(contestConfig)

}

@Singleton
class AllContestRules @Inject()(config: Config) {


   val contestNames: Seq[String] = config.getStringList("contest.contestNames").asScala.toList

   val byContestName: Map[String, ContestRules] = contestNames.map(ContestRules(_, config)).map(cr => cr.contestName -> cr).toMap

}

