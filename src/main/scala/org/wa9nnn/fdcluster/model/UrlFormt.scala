
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

import java.net.{InetAddress, URL}
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

object UrlFormt {
  implicit val  urlFormat: Format[URL] = new Format[URL] {
    override def reads(json: JsValue): JsResult[URL] = {
      val ss = json.as[String]

      try {
        JsSuccess(new URL(ss))
      }
      catch {
        case e: IllegalArgumentException ⇒ JsError(e.getMessage)
      }
    }

    override def writes(url: URL): JsValue = {
      JsString(url.toExternalForm)
    }
  }

}
object InetAddressFormat {
  implicit val  inetAddressFormat: Format[InetAddress] = new Format[InetAddress] {
    override def reads(json: JsValue): JsResult[InetAddress] = {
      try {
        JsSuccess( InetAddress.getByName(json.as[String]))
      }
      catch {
        case e: IllegalArgumentException ⇒ JsError(e.getMessage)
      }
    }

    override def writes(url: InetAddress): JsValue = {
      JsString(url.getHostName)
    }
  }

}



