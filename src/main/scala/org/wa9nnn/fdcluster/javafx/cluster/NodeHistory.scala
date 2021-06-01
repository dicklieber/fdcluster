package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.NodeAddress
import play.api.libs.json.Json

import java.nio.file.{Files, Path, StandardOpenOption}
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.io.{BufferedSource, Source}
import scala.util.Using

@Singleton
class NodeHistory @Inject()(fileContext: FileContext, config: Config = ConfigFactory.load()) extends LazyLogging {
  private val vitals = new TrieMap[NodeAddress, NodeVitals]()
  implicit val ages: Ages = Ages(config)

  /**
   * Consider a, perhaps new, node.
   *
   * @param nodeAddress of interest.
   */
  def apply(nodeAddress: NodeAddress): Unit = {
    vitals.get(nodeAddress) match {
      case Some(existing) =>
        existing.touch()
      case None =>
        // new
        val nodeVitals = new NodeVitals(nodeAddress)
        vitals.put(nodeAddress, nodeVitals)
        recordEvent(nodeVitals.joinedEvent)
    }
  }

  /**
   * Handle old and purged.
   * invoke on timer.
   *
   * @return nodes to be purged.
   */
  def grimReaper(): List[NodeAddress] = {
    val r: List[FdNodeEvent] = vitals
      .values
      .toList
      .flatMap { nodeVitals: NodeVitals =>
        nodeVitals.maybeOld().foreach(ev => recordEvent(ev))
        nodeVitals.maybeDead().map { ev =>
          recordEvent(ev)
          ev
        }
      }
    r.map { ev =>
      ev.nodeAddress
    }
  }


  def readEvents(nodeAddress: NodeAddress): List[FdNodeEvent] = {
    val file = path(nodeAddress).toFile
    Using.resource(Source.fromFile(file)) { r: BufferedSource =>
      r.getLines().map { line =>
        Json.parse(line).as[FdNodeEvent]
      }.toList
    }
  }

  private val logsDirectory: Path = fileContext.logsDirectory

  private def recordEvent(nodeEvent: FdNodeEvent): Unit = {
    logger.info(s"${nodeEvent.nodeAddress.display}: ${nodeEvent.nodeEventKind}}")

    Files.writeString(path(nodeEvent.nodeAddress), Json.toJson(nodeEvent).toString() + "\n",
      StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  }

  private def path(nodeAddress: NodeAddress): Path = {
    logsDirectory.resolve(s"node-${nodeAddress.fileUrlSafe}.json")
  }
}

case class FdNodeEvent(nodeAddress: NodeAddress, nodeEventKind: String, stamp: Instant = Instant.now())


