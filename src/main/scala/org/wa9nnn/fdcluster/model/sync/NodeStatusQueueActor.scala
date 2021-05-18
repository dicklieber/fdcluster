package org.wa9nnn.fdcluster.model.sync

import akka.actor.{Actor, ActorRef}
import com.typesafe.scalalogging.LazyLogging

/**
 * Wraps the [[NodeStatusQueue]] to provide the thread-safety of an actor.
 */
class NodeStatusQueueActor extends Actor with LazyLogging {
  private val nodeStatusQueue = new NodeStatusQueue

  override def receive: Receive = {
    case ns: NodeStatus =>
      nodeStatusQueue.add(ns)
    case NextNodeStatus =>
      val maybeStatus = nodeStatusQueue.take()
      logger.debug(s"Returning: $maybeStatus")
      sender ! maybeStatus

    case x =>
      val s: ActorRef = sender()
      logger.info(s"Unexpected message: $x from ${s.path}")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    logger.error(message.toString, reason)
    super.preRestart(reason, message)
  }

  override def postStop(): Unit = {
    logger.error("postStop")
    super.postStop()
  }
}

case object NextNodeStatus
