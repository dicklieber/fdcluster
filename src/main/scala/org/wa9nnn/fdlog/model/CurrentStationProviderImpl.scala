
package org.wa9nnn.fdlog.model

import org.wa9nnn.fdlog.model.MessageFormats.CallSign

case class CurrentStation(ourStation: OurStation = OurStation("WA9NNN", "IC-7300", "endfed"),
                           bandMode: BandMode = new BandMode("20m","digital")){
}

trait CurrentStationProvider {
  /**
   * Currently configured
   *
   * @return
   */
  def currentStation: CurrentStation
  def bandMode:BandMode = currentStation.bandMode
  def ourStation:OurStation = currentStation.ourStation
  def update(currentStation: CurrentStation):Unit
}

class CurrentStationProviderImpl extends CurrentStationProvider {
  /**
   * Currently configured
   *
   * @return
   */
  private var currentStation_ = CurrentStation()

  override def update(currentStation: CurrentStation): Unit = {
    currentStation_ = currentStation
  }

  /**
   * Currently configured
   *
   * @return
   */
  override def currentStation: CurrentStation = currentStation_
}


case class OurStation(operator: CallSign, rig: String = "", antenna: String = "")


