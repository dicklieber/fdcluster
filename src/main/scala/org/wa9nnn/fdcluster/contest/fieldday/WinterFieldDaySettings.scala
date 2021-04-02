
package org.wa9nnn.fdcluster.contest.fieldday

import org.wa9nnn.fdcluster.model.MessageFormats.CallSign

case class WinterFieldDaySettings(gotaCallSign:CallSign,
                                  club:String,
                                  nParticipants:Int,
                                  power:String = "150")
