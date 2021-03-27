
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

import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{Persistence, StructuredLogging}
import scalafx.beans.binding.{Bindings, ObjectBinding}
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.scene.image.Image

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success}

/**
 * Provides access and persistence of a single [[Contest]] instance.
 *
 * @param persistence saves and loads any a case class.
 */
@Singleton
class ContestProperty @Inject()(persistence: Persistence) extends ObjectProperty[Contest] with StructuredLogging {


  val contest: Contest = persistence.loadFromFile[Contest](() => Contest())

  val callSignProperty: StringProperty = StringProperty(contest.callSign)

  def callSign: String = callSignProperty.value

  val eventProperty: StringProperty = StringProperty(contest.event)

  def event: String = eventProperty.value

  val ourExchangeProperty: ObjectProperty[Exchange] = ObjectProperty[Exchange](contest.ourExchange)

  def ourExchange: Exchange = ourExchangeProperty.value

  val eventYearProperty: StringProperty = StringProperty(contest.year)

  def eventYear: String = eventYearProperty.value

  val logotypeImageProperty: ObjectProperty[Image] = new ObjectProperty[Image]()

  def fileBase: String = event

  callSignProperty.onChange { (_, old, nv) =>
    println(s"callSignProperty changed from : $old to: $nv ")
  }
  onChange { (_, old, nv) =>
    println(s"contestProperty changed from : $old to: $nv ")
  }

  logotypeImageProperty.onChange { (_, _, ni) =>
    println(s"logotypeImageProperty: $ni")
  }

  /**
   * Responds to a change on any of the property objects
   */
  val b: ObjectBinding[Contest] = Bindings.createObjectBinding(
    () => {
      val externalForm: String = getClass.getResource(s"/images/${eventProperty.value}.png").toExternalForm
      val image = new Image(externalForm, 150.0, 150.0, true, true)
      logotypeImageProperty.setValue(image)

      val newContest = Contest(callSignProperty.value, ourExchangeProperty.value, eventProperty.value, eventYearProperty.value)
      newContest
    }
    ,
    callSignProperty, eventProperty, ourExchangeProperty, eventYearProperty
  )

  b.onChange {
    (_, _, nv) =>
      value = nv
  }

  def save(): Unit = {
    persistence.saveToFile(value) match {
      case Failure(exception) =>
        logger.error("Saving ContestProperty", exception)
      case Success(value) =>
    }
  }


}

