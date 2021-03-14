
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

import javax.inject.Inject

/**
 * Identifies one node in the cluster.
 * For testing there can be more than one node at an IP address. So the instance is used to qualify them.
 * This is not suitable to send messages to a node. Thats in org.wa9nnn.fdcluster.model.sync.NodeStatus#apiUrl()
 *
 * @param instance from "instance in application.conf. Can be overridden on the command line. e.g. -Dinstance=2
 * @param nodeAddress
 */
case class NodeAddress @Inject()(instance: Int = 0, nodeAddress: String = "localhost") extends Ordered[NodeAddress] {
  def display: String = {
    s"$nodeAddress:$instance"
  }
  def graphiteName:String = {
    s"${nodeAddress.replace('.', '_')}:$instance"
  }

  override def compare(that: NodeAddress): Int = {
    var ret = this.nodeAddress compareTo that.nodeAddress
    if (ret == 0) {
      ret = this.instance compareTo that.instance
    }
    ret
  }
}
