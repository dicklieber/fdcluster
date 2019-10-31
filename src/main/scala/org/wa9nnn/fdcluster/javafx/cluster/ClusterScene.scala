
package org.wa9nnn.fdcluster.javafx.cluster

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.store.StoreActor.DumpCluster
import org.wa9nnn.fdcluster.store.network.cluster.NodeStateContainer
import scalafx.scene.Node

import scala.concurrent.Await

/**
 * Create JavaFX UI to view status of each node in the cluster.
 */
class ClusterScene @Inject()(@Inject() @Named("store") store: ActorRef) extends LazyLogging {

  private val clusterTable = new ClusterTable
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

 // refresh()

  def refresh(): Unit = {
    val future = store ? DumpCluster
    val clusters: Iterable[NodeStateContainer] = Await.result(future, timeout.duration).asInstanceOf[Iterable[NodeStateContainer]]
   clusterTable.refresh(clusters)
  }


  val pane: Node = clusterTable.tableView
}
