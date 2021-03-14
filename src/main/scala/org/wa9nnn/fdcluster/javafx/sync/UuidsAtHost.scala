
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

import java.net.URL

import org.wa9nnn.fdcluster.http.HttpRequestGenerator
import org.wa9nnn.fdcluster.model.MessageFormats.Uuid
import org.wa9nnn.fdcluster.model.{NodeAddress, QsoRecord}
import org.wa9nnn.fdcluster.store.network.FdHour

/**
 *
 * @param fdHours empty for all FdHours
 */
case class RequestUuidsForHour(url: URL, fdHours:List[FdHour] = List.empty,  path: String = "qsos") extends HttpRequestGenerator
/**
 *
 * @param nodeAddress where this came from. //TODO do we need this
 * @param uuids on this node for requested FdHours (or all)
 */
case class UuidsAtHost(nodeAddress: NodeAddress, uuids: List[Uuid])

/**
 *
 * @param uuids for which we would like [[QsoRecord]]s
 */
case class QsoRequest(uuids: List[Uuid])