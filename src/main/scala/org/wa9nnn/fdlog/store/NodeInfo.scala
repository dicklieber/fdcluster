
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

package org.wa9nnn.fdlog.store

import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

import org.wa9nnn.fdlog.model.{Contest, FdLogId}


/**
 *
 * @param nodeSerialNumbers source of node serialnumbers.
 * @param nodeAddress              this node.
 */
class NodeInfoImpl(val contest: Contest,
                   val nodeSerialNumbers: AtomicInteger = new AtomicInteger(),
                   val nodeAddress: String = InetAddress.getLocalHost.getHostAddress
                  ) extends NodeInfo {
  override def nextSn: Int = nodeSerialNumbers.getAndIncrement()

  def this() {
    this( Contest("FD", 2019))
  }
}

trait NodeInfo {
  def fdLogId: FdLogId = {
    FdLogId(nextSn, nodeAddress)
  }

  def nextSn: Int

  def nodeAddress: String

  def contest: Contest

}

object NodeInfo {
  type Node = String

}

