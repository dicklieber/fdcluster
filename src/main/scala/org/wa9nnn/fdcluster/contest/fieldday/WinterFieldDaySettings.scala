
package org.wa9nnn.fdcluster.contest.fieldday

import org.wa9nnn.fdcluster.model.MessageFormats.CallSign

case class WinterFieldDaySettings(gotaCallSign:CallSign = "NE9A",
                                  club:String = "WM9W",
                                  nParticipants:Int = 5,
                                  power:String = "150")
