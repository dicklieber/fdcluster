
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

import _root_.scalafx.beans.property.{ObjectProperty, _}
import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.MessageFormats._

import javax.inject.{Inject, Singleton}

/**
 * Provides access and persistence of a single [[Contest]] instance.
 *
 * @param fileContext saves and loads any a case class.
 */
@Singleton
class ContestProperty @Inject()(fileContext: FileContext) extends PersistableProperty[Contest](fileContext){


  /**
   * provide a new default instance of T. Needed when there is no file persisted/
   *
   * @return
   */
  override def defaultInstance: Contest = new Contest(nodeAddress = fileContext.nodeAddress)


  def contest: Contest = value

  def callSign: String = value.callSign

  lazy val contestNameProperty: StringProperty = new StringProperty()

  def contestName: String = contestNameProperty.value match {
    case "FD" => "ARRL-FIELD-DAY"
      // Winter Fields not in ADIF Contest ID Enumeration at least on 5/19/2021 https://adif.org/312/ADIF_312.htm#Contest_ID
    case x =>
      x
  }

  lazy val ourExchangeProperty: ObjectProperty[Exchange] = new ObjectProperty[Exchange]()

  def ourExchange: Exchange = ourExchangeProperty.value

  onChanged(value)
  /**
   * Invoked initially and when the property changes.
   */
  override def onChanged(contest: Contest): Unit = {
    contestNameProperty.value = contest.contestName
    ourExchangeProperty.value = contest.ourExchange
    okToLogProperty.value = contest.isOk
  }

}

