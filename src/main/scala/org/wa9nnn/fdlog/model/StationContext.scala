
package org.wa9nnn.fdlog.model

import org.wa9nnn.fdlog.model.Contact.CallSign
import org.wa9nnn.fdlog.store.Store

case class StationContext(
                           store: Store,
                           operator: OurStation = OurStation("WA9NNN", "IC-7300", "endfed"),
                           bandMode: BandMode
                         )


case class BandMode(band: Band, mode: Mode)

case class OurStation(operator:CallSign, rig:String = "", antenna:String = "")
