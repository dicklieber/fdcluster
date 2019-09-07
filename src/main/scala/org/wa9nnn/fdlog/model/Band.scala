
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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

case class Band(band:String){
  if (Band.bands.contains(band)) {
  }else{
    throw new IllegalArgumentException
  }
}

object Band {
  val bands: Seq[String] = Seq(
    "160m",
    "80m",
    "40m",
    "20m",
    "15m",
    "10m",
    "6m",
    "2m",
    "1.25m",
    "70cm"
  )



    implicit val  bandFormat: Format[Band] = new Format[Band] {
      override def reads(json: JsValue): JsResult[Band] = {
        val ss = json.as[String]

        try {
          JsSuccess(Band(ss))
        }
        catch {
          case e: IllegalArgumentException ⇒ JsError(e.getMessage)
        }
      }

      override def writes(mode: Band): JsValue = {
        JsString(mode.band)
      }
    }

}
