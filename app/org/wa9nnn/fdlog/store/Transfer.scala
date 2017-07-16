
package org.wa9nnn.fdlog.store

import java.time.Instant

import org.wa9nnn.fdlog.model.Contact

case class Transfer(stamp: Instant, node: String, contacts: Seq[Contact])
git status
