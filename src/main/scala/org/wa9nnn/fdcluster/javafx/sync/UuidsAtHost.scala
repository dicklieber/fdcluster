
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

package org.wa9nnn.fdcluster.javafx.sync

import org.wa9nnn.fdcluster.http.JsonRequestResponse
import org.wa9nnn.fdcluster.model.MessageFormats.{Uuid, _}
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.{JsObject, Json}

/**
 *
 * @param fdHours empty for all FdHours
 */
case class RequestUuidsForHour(fdHours: List[FdHour] = List.empty) extends JsonRequestResponse {
  override def toJson: JsObject = Json.toJson(this).as[JsObject]

  override def parseResponse(jsObject: JsObject):ResponseMessage = {
    jsObject.as[UuidsAtHost]
  }
}

/**
 *
 * @param nodeAddress where this came from. //TODO do we need this
 * @param uuids       on this node for requested FdHours (or all)
 */
case class UuidsAtHost(nodeAddress: NodeAddress,  uuids: List[Uuid]) extends UuidContainer {
  override def toString: Node = f"${uuids.length} uuids from $nodeAddress"
}







