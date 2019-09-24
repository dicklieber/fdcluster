
package org.wa9nnn.fdlog.model

import javax.inject.Inject

case class NodeAddress @Inject()(instance: Int = 0, nodeAddress: String = "localhost") extends Ordered[NodeAddress] {
  def display: String = {
    s"$nodeAddress:$instance"
  }

  override def compare(that: NodeAddress): Int = {
    var ret = this.nodeAddress compareTo that.nodeAddress
    if (ret == 0) {
      ret = this.instance compareTo that.instance
    }
    ret
  }
}
