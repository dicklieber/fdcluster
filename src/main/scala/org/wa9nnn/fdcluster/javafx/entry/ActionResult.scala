
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

import _root_.scalafx.scene.control.Label
import com.typesafe.scalalogging.LazyLogging
import io.prometheus.client.Counter
import org.scalafx.extras.{onFX, onFXAndWait}
import org.wa9nnn.fdcluster.javafx.entry.ActionResult.qsosLogged
import org.wa9nnn.fdcluster.model.Qso
import org.wa9nnn.fdcluster.store.SearchResult
import org.wa9nnn.util.WithDisposition
import play.api.libs.json.Json
import scalafx.beans.property.StringProperty

import javax.inject.Singleton
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import org.wa9nnn.fdcluster.model.MessageFormats._
@Singleton
class ActionResult extends Label("") with WithDisposition with LazyLogging {
  val tp: StringProperty = text

  def sadMessage(str: String): Unit = {
    text = str
    sad()
  }


  def apply(f: Try[Qso]): Unit = {
    onFX(f match {
      case Failure(exception) =>
        text = exception.getMessage
        sad()
      case Success(qso) =>
        qsosLogged.inc()
//todo        logger.info(Json.toJson(qso).toString())
        tp.value = s"Added ${qso.callSign}"
        happy()
    })
  }

  def apply(eventualSearchresult: Future[SearchResult]): Unit = {
    eventualSearchresult.foreach { searchResult =>
      logger.whenTraceEnabled {
        logger.trace(s"Action : $searchResult")
      }

      val value: String = searchResult.qsos.map(qso =>
        qso.callSign).mkString("\n")
      eventualSearchresult.foreach { searchResult =>
        logger.whenTraceEnabled {
          logger.trace(s"Action s: $value")
        }
        onFXAndWait {
          text = value
        }
      }
    }
  }

  def clear(): Unit = {
    text = ""
    neutral()
  }

}

object ActionResult {
  val qsosLogged: Counter = Counter.build.name("qsosLogged").help("Qsos logged.").register

}
