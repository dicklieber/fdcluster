package org.wa9nnn.fdcluster.javafx.cluster

import org.wa9nnn.fdcluster.model.NodeAddress

import java.time.{Duration, Instant}

/**
 * LIfecycle infor about a ode.
 * @param nodeAddress where
 * @param birth when it joined.
 * @param ages to calculate how old.
 */
class NodeVitals(val nodeAddress: NodeAddress, val birth: Instant = Instant.now())(implicit ages: Ages) {
  private var touched: Instant = birth
  private var alreadyReportedAsOld = false
  private var alreadyPurged= false

  def touch(): Unit = {
    touched = Instant.now()
    alreadyReportedAsOld = false
  }
  private  def ageInSeconds:Long = Duration. between(touched, Instant.now()).toSeconds

  def maybeOld(now: Instant = Instant.now()): Option[FdNodeEvent] = {
    Option.when(!alreadyReportedAsOld && ages.old(ageInSeconds)) {
      alreadyReportedAsOld = true
      oldEvent
    }
  }

  def maybeDead(now: Instant = Instant.now()): Option[FdNodeEvent] = {
    Option.when( !alreadyPurged && ages.death( ageInSeconds)){
      alreadyPurged = true
      purgeEvent
    }
  }

  def joinedEvent: FdNodeEvent = FdNodeEvent(nodeAddress, "joined")

  private def oldEvent: FdNodeEvent = FdNodeEvent(nodeAddress, "old")

  private def purgeEvent: FdNodeEvent = FdNodeEvent(nodeAddress, "purged")
}
