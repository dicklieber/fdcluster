
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

import _root_.scalafx.beans.binding.{Bindings, ObjectBinding}
import _root_.scalafx.scene.image.Image
import com.wa9nnn.util.macos.DockIcon
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{Persistence, StructuredLogging}
import scalafx.beans.property._

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Using}

/**
 * Provides access and persistence of a single [[Contest]] instance.
 *
 * @param persistence saves and loads any a case class.
 */
@Singleton
class ContestProperty @Inject()(persistence: Persistence) extends ObjectProperty[Contest] with StructuredLogging {


  private val initContest: Contest = persistence.loadFromFile[Contest](() => Contest())
  value = initContest

  def contest: Contest = value

  val callSignProperty: StringProperty = StringProperty(initContest.callSign)

  def callSign: String = callSignProperty.value

  val eventProperty: StringProperty = StringProperty(initContest.eventName)

  def event: String = eventProperty.value

  val ourExchangeProperty: ObjectProperty[Exchange] = ObjectProperty[Exchange](initContest.ourExchange)

  def ourExchange: Exchange = ourExchangeProperty.value

  val eventYearProperty: StringProperty = StringProperty(initContest.year)

  def eventYear: String = eventYearProperty.value

  val logotypeImageProperty: ObjectProperty[Image] = new ObjectProperty[Image]()
  setUpImage(initContest.eventName)

  def fileBase: String = event

  callSignProperty.onChange { (_, old, nv) =>
    println(s"callSignProperty changed from : $old to: $nv ")
  }
  onChange { (_, old, contest) =>
    setUpImage(contest.eventName)
    whenTraceEnabled(() => s"contestProperty changed from : $old to: $contest ")
  }

  logotypeImageProperty.onChange { (_, _, ni) =>
    println(s"logotypeImageProperty: $ni")
  }

  /**
   * Responds to a change on any of the property objects
   */
  val b: ObjectBinding[Contest] = Bindings.createObjectBinding(
    () => {
      val newContest = Contest(callSignProperty.value, ourExchangeProperty.value, eventProperty.value, eventYearProperty.value)
      newContest
    }
    ,
    callSignProperty, eventProperty, ourExchangeProperty, eventYearProperty
  )

  def setUpImage(eventName: String): Unit = {
    val imagePath: String = s"/images/$eventName.png"
    Using(getClass.getResourceAsStream(imagePath)) { is =>
      new Image(is, 150.0, 150.0, true, true)
    } match {
      case Failure(exception) =>
        logger.error(s"loading: $imagePath", exception)
      case Success(image) =>
        logotypeImageProperty.setValue(image)
    }

    try {
      {
//        DockIcon(imagePath)
      }
    } catch {
      case e: java.lang.NoClassDefFoundError =>
        logger.debug("Icon switch", e)
      case et:Throwable =>
        logger.debug("Icon switch", et)

    }
  }

  b.onChange {
    (_, _, nv: Contest) =>
      value = nv
  }

  def save(): Unit = {
    persistence.saveToFile(value) match {
      case Failure(exception) =>
        logger.error("Saving ContestProperty", exception)
      case Success(path) =>
        whenDebugEnabled { () =>
          s"Saved $value to $path."
        }
    }
  }


}

