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

package org.wa9nnn.fdcluster.javafx.entry

import org.wa9nnn.fdcluster.javafx.entry.section.Section

import scala.language.postfixOps

object Sections {

  def find(partial: String): Seq[Section] = {
    sections.filter(_.code.startsWith(partial))
  }

  //todo Load from a file
  val sections: Seq[Section] = Seq(
    Section("Connecticut", "CT", "1"),
    Section("Eastern Massachusetts", "EMA", "1"),
    Section("Maine", "ME", "1"),
    Section("New Hampshire", "NH", "1"),
    Section("Rhode Island", "RI", "1"),
    Section("Vermont", "VT", "1"),
    Section("Western Massachusetts", "WMA", "1"),
    Section("Eastern New York", "ENY", "2"),
    Section("New York City - Long Island", "NLI", "2"),
    Section("Northern New Jersey", "NNJ", "2"),
    Section("Northern New York", "NNY", "2"),
    Section("Southern New Jersey", "SNJ", "2"),
    Section("Western New York", "WNY", "2"),
    Section("Delaware", "DE", "3"),
    Section("Eastern Pennsylvania", "EPA", "3"),
    Section("Maryland-DC", "MDC", "3"),
    Section("Western Pennsylvania", "WPA", "3"),
    Section("Alabama", "AL", "4"),
    Section("Georgia", "GA", "4"),
    Section("Kentucky", "KY", "4"),
    Section("North Carolina", "NC", "4"),
    Section("Northern Florida", "NFL", "4"),
    Section("South Carolina", "SC", "4"),
    Section("Southern Florida", "SFL", "4"),
    Section("West Central Florida", "WCF", "4"),
    Section("Tennessee", "TN", "4"),
    Section("Virginia", "VA", "4"),
    Section("Puerto Rico", "PR", "4"),
    Section("Virgin Islands", "VI", "4"),
    Section("Arkansas", "AR", "5"),
    Section("Louisiana", "LA", "5"),
    Section("Mississippi", "MS", "5"),
    Section("New Mexico", "NM", "5"),
    Section("North Texas", "NTX", "5"),
    Section("Oklahoma", "OK", "5"),
    Section("South Texas", "STX", "5"),
    Section("West Texas", "WTX", "5"),
    Section("East Bay", "EB", "6"),
    Section("Los Angeles", "LAX", "6"),
    Section("Orange", "ORG", "6"),
    Section("Santa Barbara", "SB", "6"),
    Section("Santa Clara Valley", "SCV", "6"),
    Section("San Diego", "SDG", "6"),
    Section("San Francisco", "SF", "6"),
    Section("San Joaquin Valley", "SJV", "6"),
    Section("Sacramento Valley", "SV", "6"),
    Section("Pacific", "PAC", "6"),
    Section("Arizona", "AZ", "7"),
    Section("Eastern Washington", "EWA", "7"),
    Section("Idaho", "ID", "7"),
    Section("Montana", "MT", "7"),
    Section("Nevada", "NV", "7"),
    Section("Oregon", "OR", "7"),
    Section("Utah", "UT", "7"),
    Section("Western Washington", "WWA", "7"),
    Section("Wyoming", "WY", "7"),
    Section("Alaska", "AK", "7"),
    Section("Michigan", "MI", "8"),
    Section("Ohio", "OH", "8"),
    Section("West Virginia", "WV", "8"),
    Section("Illinois", "IL", "9"),
    Section("Indiana", "IN", "9"),
    Section("Wisconsin", "WI", "9"),
    Section("Colorado", "CO", "0"),
    Section("Iowa", "IA", "0"),
    Section("Kansas", "KS", "0"),
    Section("Minnesota", "MN", "0"),
    Section("Missouri", "MO", "0"),
    Section("Nebraska", "NE", "0"),
    Section("North Dakota", "ND", "0"),
    Section("South Dakota", "SD", "0"),
    Section("Maritime", "MAR", "CA"),
    Section("Newfoundland/Labrador", "NL", "CA"),
    Section("Quebec", "QC", "CA"),
    Section("Ontario East", "ONE", "CA"),
    Section("Ontario North", "ONN", "CA"),
    Section("Ontario South", "ONS", "CA"),
    Section("Greater Toronto Area", "GTA", "CA"),
    Section("Manitoba", "MB", "CA"),
    Section("Saskatchewan", "SK", "CA"),
    Section("Alberta", "AB", "CA"),
    Section("British Columbia", "BC", "CA"),
    Section("Northern Territories", "NT", "CA"),
    Section("Outside US/Canada", "DX",  "DX")
  ).sorted


  val byCode: Map[String, Section] = sections.map(s ⇒ s.code → s) toMap

  def isValid(candidate: String): Boolean = byCode.contains(candidate)

  val sortedByCode: Seq[Section] = sections.sortBy(_.code)
  val defaultSection: Section = sortedByCode.head
  val defaultCode: String = {
    defaultSection.code
  }

  val byArea: Map[String, String] = sections.map(section => section.code -> section.area).toMap
  def callAreaForSection(sectionName:String):String = {
    byArea(sectionName)
  }
}