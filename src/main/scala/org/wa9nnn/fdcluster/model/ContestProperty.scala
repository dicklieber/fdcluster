
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

import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.Persistence
import scalafx.beans.binding.{Bindings, ObjectBinding}
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}

import java.time.LocalDate
import javax.inject.Inject

/**
 * Provides access and persistence of a single [[Contest]] instance.
 *
 * @param persistence saves and loads any a case class.
 */
class ContestProperty @Inject()(persistence: Persistence) {
  private val current = persistence.loadFromFile[Contest].getOrElse(Contest())

  val contestProperty: ObjectProperty[Contest] = ObjectProperty(current)

  val contest: Contest = contestProperty.value
  val callSignProperty: StringProperty = StringProperty(current.callSign)
  val callSign: String = callSignProperty.value
  val eventProperty: StringProperty = StringProperty(current.event)
  val event: String = eventProperty.value
  val ourExchangeProperty: ObjectProperty[Exchange] = ObjectProperty(current.ourExchange)
  val ourExchange: Exchange = ourExchangeProperty.value
  val eventYearProperty: StringProperty = StringProperty(current.year)
  val eventYear: String = eventYearProperty.value

  callSignProperty.onChange{(_,old,nv) =>
    println(s"callSignProperty changed from : $old to: $nv ")
  }
  contestProperty.onChange{(_,old,nv) =>
    println(s"contestProperty changed from : $old to: $nv ")
  }
  /**
   * Responds to a change on any of the property objects
   */
  val b: ObjectBinding[Contest] = Bindings.createObjectBinding(
    () => {
      val newContest = Contest(callSignProperty.value, ourExchangeProperty.value, eventProperty.value, eventYearProperty.value)
      newContest
    },
    callSignProperty, eventProperty, ourExchangeProperty, eventYearProperty
  )

  b.onChange { (_, _, nv) =>
    contestProperty.value = nv
  }

  def save():Unit = {
    persistence.saveToFile(contestProperty.value)
  }
}

/**
 * Information needed about the contest.
 * Should not change over the durtion of the contest.
 *
 * @param callSign    who we are. Usually the clubs callsign.
 * @param ourExchange what we will send to worked stations.
 * @param event       which contest. We only support FD and Winter Field Day.
 * @param year        which one.
 */
case class Contest(callSign: CallSign = "",
                   ourExchange: Exchange = new Exchange(),
                   event: String = "FD",
                   year: String = {
                     LocalDate.now().getYear.toString
                   }) {

//  override def toString: String = {
//    s"$event-$year"
//  }

  lazy val toId: String = {
    f"$event$year$callSign"
  }
}