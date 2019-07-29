
/*
 * Copyright (C) 2017  Dick Lieber, WA9NNN
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wa9nnn.fdlog.model

import java.time.LocalDate

import play.api.libs.json._

case class Contest(event: String, year: Int = {
  LocalDate.now().getYear
}) {
  override def toString: String = {
    s"$event:$year"
  }
}

object Contest {
  private val r = """(.*):(\d{4})""".r

  def apply(in: String): Contest = {
    in match {
      case r(c, year) ⇒
        Contest(c, year.toInt)
      case _ ⇒
        throw new IllegalArgumentException(s"Can't parse: $in")
    }
  }

  implicit val modeFormat: Format[Contest] = new Format[Contest] {
    override def reads(json: JsValue): JsResult[Contest] = {
      val ss = json.as[String]

      try {
        JsSuccess(Contest(ss))
      }
      catch {
        case e: IllegalArgumentException ⇒ JsError(e.getMessage)
      }
    }

    override def writes(contest: Contest): JsValue = {
      JsString(contest.toString)
    }
  }

}
