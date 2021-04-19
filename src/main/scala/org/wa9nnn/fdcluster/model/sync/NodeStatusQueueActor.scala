package org.wa9nnn.fdcluster.model.sync

import akka.actor.Actor
import org.wa9nnn.util.StructuredLogging

/**
 * Wraps the [[NodeStatusQueue]] to provide the thread-safety of an actor.
 */
class NodeStatusQueueActor extends Actor with StructuredLogging{
  private val nodeStatusQueue = new NodeStatusQueue

  override def receive: Receive = {
    case ns: NodeStatus =>
      nodeStatusQueue.add(ns)
    case NextNodeStatus =>
    val maybeStatus = nodeStatusQueue.take()
      logger.debug(s"Returning: $maybeStatus")
      sender ! maybeStatus
  }
}

case object NextNodeStatus
