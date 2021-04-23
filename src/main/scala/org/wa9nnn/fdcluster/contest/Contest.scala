
package org.wa9nnn.fdcluster.contest

import org.wa9nnn.fdcluster.model.{Exchange, NodeAddress}
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign

import java.time.{Instant, LocalDate}


/**
 * Information needed about the contest.
 * Should not change over the durtion of the contest.
 *
 * @param callSign    who we are. Usually the clubs callSign.
 * @param ourExchange what we will send to worked stations.
 * @param contestName       which contest. We only support FD and Winter Field Day.
 * @param year        which one.
 */
case class Contest(callSign: CallSign = "",
                   ourExchange: Exchange =  Exchange(),
                   contestName: String = "FieldDay",
                   nodeAddress: NodeAddress = NodeAddress(),
                   stamp:Instant = Instant.now()
                  ) {

  def fileBase: String = {
    s"$contestName-${LocalDate.now().getYear.toString}"
  }

  val id:String = contestName.filter(_.isUpper)
  def qsoId: String = {
    f"$id$callSign"
  }
}



