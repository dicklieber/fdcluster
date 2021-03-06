
package org.wa9nnn.fdcluster.contest

import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import org.wa9nnn.fdcluster.model.{Exchange, NodeAddress, OkContributor, Stamped}

import java.time.{Instant, LocalDate}


/**
 * Information needed about the contest.
 * Should not change over the durtion of the contest.
 *
 * @param callSign    who we are. Usually the clubs callSign.
 * @param ourExchange what we will send to worked stations.
 * @param contestName which contest. We only support FD and Winter Field Day.
 * @param year        which one.
 */
case class Contest(callSign: CallSign = "",
                   ourExchange: Exchange = Exchange(),
                   contestName: String = "FieldDay",
                   nodeAddress: NodeAddress = NodeAddress(),
                   password: String = "",
                   stamp: Instant = Instant.now(),
                  ) extends Stamped[Contest]with OkContributor {


  def checkValid(): Unit = {
    if (!isOk) {
      throw new IllegalStateException(s"No CallSign!")
    }
  }

  def isOk: Boolean = {
    callSign.nonEmpty
  }

  def fileBase: String = {
    s"$contestName-${LocalDate.now().getYear.toString}"
  }

  val id: String = contestName.filter(_.isUpper)

  def qsoId: String = {
    f"$id$callSign"
  }

  override def updateOk(): Unit = {
    OkGate(OkItem("Contest", "callSign", "Must have entrant CallSign!") { () => isOk })

  }
}

