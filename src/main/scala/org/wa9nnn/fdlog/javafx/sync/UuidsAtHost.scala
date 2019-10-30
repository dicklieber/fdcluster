
package org.wa9nnn.fdlog.javafx.sync

import java.net.URL

import org.wa9nnn.fdlog.http.HttpRequestGenerator
import org.wa9nnn.fdlog.model.MessageFormats.Uuid
import org.wa9nnn.fdlog.model.NodeAddress
import org.wa9nnn.fdlog.store.network.FdHour

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
 * @param uuids for which we would like [[org.wa9nnn.fdlog.model.QsoRecord]]s
 */
case class QsoRequest(uuids: List[Uuid])