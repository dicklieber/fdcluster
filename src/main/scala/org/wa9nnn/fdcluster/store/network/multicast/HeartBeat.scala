package org.wa9nnn.fdcluster.store.network.multicast

import akka.actor.{ActorSystem, Cancellable, Scheduler}
import com.github.racc.tscg.TypesafeConfig
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.store.network.JsonContainerSender
import org.wa9nnn.fdcluster.store.{JsonContainer, RequestNodeStatus, StoreSender}

import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Manages sending heartbeat message whenever no other message has been sent to  too  long.
 *
 * @param storeSender       how we get a [[NodeStatus]]
 * @param actorSystem       where this high [[akka.actor.Scheduler]] lives.
 * @param sender            to send messages.
 * @param heartBeatDuration from application.conf.
 */
@Singleton
class HeartBeat @Inject()(storeSender: StoreSender,
                          actorSystem: ActorSystem,
                          sender: JsonContainerSender,
                          @TypesafeConfig("fdcluster.multicast.heartbeat") heartBeatDuration: Duration,
                         ) extends LazyLogging {


  private val scheduler: Scheduler = actorSystem.getScheduler
  private var cancellable: Option[Cancellable] = None


  apply { () =>
    logger.trace("init heartbeat")
    // do nothing just get timer  going.
  }

  def apply(f: () => Unit): Unit = {
    cancellable.foreach { c =>
      c.cancel()
      cancellable = None
    }
    try {
      f()
    } catch {
      case e: Exception =>
        logger.error("In heartbeat", e)
    } finally {
      cancellable = Option(scheduler.scheduleOnce(heartBeatDuration, new Runnable {
        override def run(): Unit = {
          logger.whenTraceEnabled{
            logger.trace(s"Timeup; sending heatbeat")
          }
          sendHeartBeat() // this will reschedule.
        }
      }
      ))
    }
  }

  private def sendHeartBeat(): Unit = {
    try {
      logger.trace("Heartbeat")
      val eventualNodeStatus: Future[NodeStatus] = (storeSender ?[NodeStatus] RequestNodeStatus).mapTo[NodeStatus]
      eventualNodeStatus.onComplete {
        case Success(nodeStatus) =>
          sender.send(JsonContainer(nodeStatus))
        case Failure(exception) =>
          logger.error(s"Waiting for nodeStatus", exception)
      }
    } catch {
      case e: Exception =>
        logger.error(s"Sending heartbeat!", e)
    }
  }
}
