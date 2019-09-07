
package org.wa9nnn.fdlog.model.sync

import java.security.MessageDigest
import java.time.{Instant, LocalDateTime}

import org.wa9nnn.fdlog.model.MessageFormats.Uuid
import org.wa9nnn.fdlog.model.QsoRecord

case class NodeStatus(nodeAddress: String, count: Int, qsoIds: List[QsoHourIds], stamp: Instant = Instant.now()) {
}
