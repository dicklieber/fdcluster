
package org.wa9nnn.fdlog.javafx.sync

import org.wa9nnn.fdlog.model.MessageFormats.Uuid
import org.wa9nnn.fdlog.model.NodeAddress
import org.wa9nnn.fdlog.store.network.FdHour

/**
 *
 * @param fdHours empty for all FdHours
 */
case class UuidRequest(fdHours:List[FdHour] = List.empty)

/**
 *
 * @param nodeAddress where this came from. //TODO do we need this
 * @param uuids on this node for requested FdHours (or all)
 * @param requested what was asked for
 */
case class UuidsAtHost(nodeAddress: NodeAddress, uuids: List[Uuid], requested:UuidRequest)