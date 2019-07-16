
package org.wa9nnn.fdlog.model

import org.wa9nnn.fdlog.model.Contact.CallSign
import org.wa9nnn.fdlog.store.Store

case class StationContext(
                           store: Store,
                           station: Station = Station(new CallSign("WA9NNN"), Band("20m"), Mode.digital))
