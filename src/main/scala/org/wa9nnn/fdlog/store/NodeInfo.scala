
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

import java.util.concurrent.atomic.AtomicInteger

import org.wa9nnn.fdlog.model.{Contest, FdLogId, NodeAddress}
import sun.jvmstat.monitor.{MonitoredHost, MonitoredVmUtil}

import scala.collection.JavaConverters._


/**
 *
 * @param nodeSerialNumbers source of node serialnumbers.
 * @param nodeAddress              this node.
 */
class NodeInfoImpl(val contest: Contest,
                   val nodeSerialNumbers: AtomicInteger = new AtomicInteger(),
                   val nodeAddress: NodeAddress = NodeInfoImpl.isUs
                  ) extends NodeInfo {
  override def nextSn: Int = nodeSerialNumbers.getAndIncrement()

  def this() {
    this( Contest("FD", 2019))
  }

}
object NodeInfoImpl{
  def isUs:NodeAddress = {
    val mh = MonitoredHost.getMonitoredHost(null.asInstanceOf[String])
    val ids = mh.activeVms().asScala
    val  mainClasses = ids.toList.map { vmId ⇒
      val vmidString = "//" + vmId + "?mode=r"
      val aVmId = new sun.jvmstat.monitor.VmIdentifier(vmidString)
      val vm = mh.getMonitoredVm(aVmId)
      val vmIdentifier = vm.getVmIdentifier
      val mainClass = MonitoredVmUtil.mainClass(vm, false)
      println(s"""mainClass: "$mainClass"""")
      mainClass
    }
   val fdLogCount = mainClasses.count(mainClass ⇒
      mainClass == "FdLog")
    NodeAddress( fdLogCount)
  }

}

trait NodeInfo {
  def fdLogId: FdLogId = {
    FdLogId(nextSn, nodeAddress)
  }

  def nextSn: Int

  def nodeAddress: NodeAddress

  def contest: Contest

}

object NodeInfo {
  type Node = String

}

