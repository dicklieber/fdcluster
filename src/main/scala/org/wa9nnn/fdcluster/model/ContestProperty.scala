
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
import _root_.scalafx.scene.image.Image
import com.wa9nnn.util.macos.DockIcon
import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.contest.{Contest, OkToLogContributer}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.StructuredLogging

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Using}

/**
 * Provides access and persistence of a single [[Contest]] instance.
 *
 * @param fileContext saves and loads any a case class.
 */
@Singleton
class ContestProperty @Inject()(fileContext: FileContext) extends ObjectProperty[Contest]
  with OkToLogContributer with StructuredLogging {


  private val initContest: Contest = fileContext.loadFromFile[Contest](() => Contest(nodeAddress = fileContext.nodeAddress))
  value = initContest

  def contest: Contest = value

  def callSign: String = value.callSign

  val contestNameProperty: StringProperty = StringProperty(initContest.contestName)

  def contestName: String = contestNameProperty.value

  val ourExchangeProperty: ObjectProperty[Exchange] = ObjectProperty[Exchange](initContest.ourExchange)

  def ourExchange: Exchange = ourExchangeProperty.value

  val logotypeImageProperty: ObjectProperty[Image] = new ObjectProperty[Image]()

  onChange { (_, old, contest) =>
    contestNameProperty.value = contest.contestName
    ourExchangeProperty.value = contest.ourExchange

    setUpImage(contest.contestName)
    whenTraceEnabled(() => s"contestProperty changed from : $old to: $contest ")

    contestOK
  }


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
        DockIcon(imagePath)
      }
    } catch {
      case e: java.lang.NoClassDefFoundError =>
        logger.debug("Icon switch", e)
      case et: Throwable =>
        logger.debug("Icon switch", et)

    }
  }

  def save(contest: Contest): Unit = {
    value = contest
    fileContext.saveToFile(value) match {
      case Failure(exception) =>
        logger.error("Saving ContestProperty", exception)
      case Success(path) =>
        whenDebugEnabled { () =>
          s"Saved $value to $path."
        }
    }
  }

  def saveIfNewer(contest: Contest): Unit = {
    if (contest.stamp.isAfter(value.stamp)) {
      logger.info(s"New Contest from ${contest.nodeAddress.display}")
      save(contest)
    }
  }

  override def update(v: Contest): Unit =
    throw new IllegalStateException("Use save or saveIfNewer!")

  override val okToLogProperty: BooleanProperty = new BooleanProperty {

  }

  def contestOK(): Unit = {
    val value1 = value
      okToLogProperty.value = Option(value1).exists(_.isOk)
  }
  contestOK()
}

