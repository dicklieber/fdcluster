
package org.wa9nnn.fdlog.javafx.cluster

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.store.StoreActor.DumpCluster
import org.wa9nnn.fdlog.store.network.cluster.NodeStateContainer
import scalafx.scene.Node
import scalafx.scene.control.TableView

import scala.concurrent.Await

/**
 * Create JavaFX UI to view status of each node in the cluster.
 */
class ClusterScene @Inject()(@Inject() @Named("store") store: ActorRef) extends LazyLogging {

  private val clusterTable = new ClusterTable
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  private var tableView: TableView[Row] = _

  refresh()

  def refresh(): Unit = {
    val future = store ? DumpCluster
    val clusters = Await.result(future, timeout.duration).asInstanceOf[Iterable[NodeStateContainer]]
   clusterTable.refresh(clusters)
  }


  val pane: Node = clusterTable.tableView
}
