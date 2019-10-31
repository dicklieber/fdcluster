
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

package org.wa9nnn.fdcluster.store

import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

import javax.inject.Inject
import org.wa9nnn.fdcluster.model
import org.wa9nnn.fdcluster.model.{Contest, FdLogId, NodeAddress}


/**
 *
 * @param nodeSerialNumbers        source of node serialnumbers.
 * @param nodeAddress              this node.
 */
class NodeInfoImpl @Inject()(val contest: Contest,
                             val nodeAddress: NodeAddress,
                             val url: URL = new URL("http://dummy"),
                             val nodeSerialNumbers: AtomicInteger = new AtomicInteger()
                            ) extends NodeInfo {
  override def nextSn: Int = nodeSerialNumbers.getAndIncrement()
}


trait NodeInfo {
  def fdLogId: FdLogId = {
    model.FdLogId(nextSn, nodeAddress)
  }

  def nextSn: Int

  def nodeAddress: NodeAddress

  def contest: Contest
  def url:URL

}

object NodeInfo {
  type Node = String

}

