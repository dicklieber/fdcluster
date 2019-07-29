
package org.wa9nnn.fdlog.model

import org.wa9nnn.fdlog.model.Contact.CallSign

case class CurrentStation(
                           ourStation: OurStation = OurStation("WA9NNN", "IC-7300", "endfed"),
                           bandMode: BandMode = BandMode(Band("20m"), Mode.digital)
                         )

trait CurrentStationProvider {
  /**
   * Currently configured
   *
   * @return
   */
  def stationContext: CurrentStation
}

class CurrentStationProviderImpl extends CurrentStationProvider {
  /**
   * Currently configured
   *
   * @return
   */
  override val stationContext: CurrentStation = CurrentStation()
}

case class BandMode(band: Band, mode: Mode)

case class OurStation(operator: CallSign, rig: String = "", antenna: String = "")
