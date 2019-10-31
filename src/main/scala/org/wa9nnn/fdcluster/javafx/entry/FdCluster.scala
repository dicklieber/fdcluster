
package org.wa9nnn.fdcluster.javafx.entry

import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._
import org.wa9nnn.fdcluster.Module
import org.wa9nnn.fdcluster.http.Server
import org.wa9nnn.fdcluster.javafx.cluster.ClusterScene
import org.wa9nnn.fdcluster.javafx.data.DataScene
import org.wa9nnn.fdcluster.javafx.menu.FdLogMenu
import org.wa9nnn.fdcluster.model.Contest
import org.wa9nnn.fdcluster.store.NodeInfo
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.Event
import scalafx.scene.Scene
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.layout.BorderPane

/**
 * Main for FDLog
 */
object FdCluster extends JFXApp {
//  val system: ActorSystem = ActorSystem("FdLogAkka")

  private val injector = Guice.createInjector(new Module())
  private val contest = Contest("WFD", 2019)
//  implicit val nodeInfo: NodeInfo = new NodeInfoImpl(contest)
//  private val storeActorRef: ActorRef = injector.getInstance(Key.get(classOf[ActorRef], Names.named("store")))
  private val entryScene = injector.instance[EntryScene]
  private val dataScene = injector.instance[DataScene]
  private val clusterScene = injector.instance[ClusterScene]
  private val nodeInfo: NodeInfo = injector.instance[NodeInfo]
  try {
    injector.instance[Server]
  } catch {
    case e:Throwable ⇒
      e.printStackTrace()
  }
  val fdlogmenu: FdLogMenu = injector.instance[FdLogMenu]

  private val dataTab: Tab = new Tab {
    text = "Data"
    content = dataScene.pane
    closable = false
  }
  private val entryTab: Tab = new Tab {
    text = "Entry"
    content = entryScene.pane
    closable = false
  }
  private val clusterTab: Tab = new Tab {
    text = "Cluster"
    content = clusterScene.pane
    closable = false
  }
  val tabPane: TabPane = new TabPane {
    tabs = Seq(entryTab, dataTab, clusterTab)
  }

//  dataTab.onSelectionChanged = (_: Event) => {
//    if (dataTab.isSelected) {
//      dataScene.refresh()
//    }
//  }
  clusterTab.onSelectionChanged = (_: Event) => {
    if (clusterTab.isSelected) {
      clusterScene.refresh()
    }
  }
//  private val statsHeader = new HBox(Label(f"QSOs:  todo "))
  private val rootPane = new BorderPane {
    top = fdlogmenu.menuBar
    center = tabPane
  }
  val ourScene = new Scene()

  ourScene.getStylesheets.add(getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm)

  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm
  ourScene.getStylesheets.add(cssUrl)

  ourScene.root = rootPane

  stage = new PrimaryStage() {
    title = "FDCluster @ " + nodeInfo.nodeAddress.display
    scene = ourScene
  }

}
