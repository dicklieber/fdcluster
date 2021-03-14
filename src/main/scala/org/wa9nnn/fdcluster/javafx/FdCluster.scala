
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.javafx

import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._
import org.wa9nnn.fdcluster.Module
import org.wa9nnn.fdcluster.http.Server
import org.wa9nnn.fdcluster.javafx.cluster.ClusterScene
import org.wa9nnn.fdcluster.javafx.data.DataScene
import org.wa9nnn.fdcluster.javafx.entry.{EntryScene, RunningTaskPane}
import org.wa9nnn.fdcluster.javafx.menu.FdClusterMenu
import org.wa9nnn.fdcluster.model.Contest
import org.wa9nnn.fdcluster.store.NodeInfo
import org.wa9nnn.util.StructuredLogging
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.event.Event
import scalafx.scene.Scene
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, VBox}

/**
 * Main for FDLog
 */
object FdCluster extends JFXApp  with StructuredLogging {


  private val injector = Guice.createInjector(new Module(parameters))
//  implicit val nodeInfo: NodeInfo = new NodeInfoImpl(contest)
//  private val storeActorRef: ActorRef = injector.getInstance(Key.get(classOf[ActorRef], Names.named("store")))
  private val entryScene = injector.instance[EntryScene]
  private val dataScene = injector.instance[DataScene]
  private val clusterScene = injector.instance[ClusterScene]
  private val nodeInfo: NodeInfo = injector.instance[NodeInfo]
  private val runningTaskPane: RunningTaskPane = injector.instance[RunningTaskPane]
  private val statusPane: StatusPane = injector.instance[StatusPane]
  try {
    injector.instance[Server]
  } catch {
    case e:Throwable ⇒
      e.printStackTrace()
  }
  val fdlogmenu: FdClusterMenu = injector.instance[FdClusterMenu]

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
    bottom =  new VBox(
      runningTaskPane.pane,
     statusPane.pane
    )
  }
  val ourScene = new Scene()

  ourScene.getStylesheets.add(getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm)

  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm
  ourScene.getStylesheets.add(cssUrl)

  ourScene.root = rootPane

  stage = new PrimaryStage() {
    title = "FDCluster @ " + nodeInfo.nodeAddress.display
    scene = ourScene
    private val externalForm: String = getClass.getResource("/images/wfdlogo.png").toExternalForm
    icons += new Image(externalForm)
    onCloseRequest =  {event =>
      Platform.exit()
      System.exit(0)

    }
  }
}
