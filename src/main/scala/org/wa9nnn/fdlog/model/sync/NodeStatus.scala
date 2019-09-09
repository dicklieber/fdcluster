
package org.wa9nnn.fdlog.model.sync

import java.time.Instant

import org.wa9nnn.fdlog.model.NodeAddress

case class NodeStatus(nodeAddress: NodeAddress, count: Int, qsoIds: List[QsoHourIds], stamp: Instant = Instant.now()) {
}
