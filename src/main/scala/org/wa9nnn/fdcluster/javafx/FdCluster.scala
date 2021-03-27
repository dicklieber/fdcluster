
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
import org.wa9nnn.fdcluster.javafx.cluster.ClusterTab
import org.wa9nnn.fdcluster.javafx.data.DataScene
import org.wa9nnn.fdcluster.javafx.entry.{EntryScene, RunningTaskPane, StatisticsTab}
import org.wa9nnn.fdcluster.javafx.menu.FdClusterMenu
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty, NodeAddress}
import org.wa9nnn.util.{CommandLine, StructuredLogging}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, GridPane, HBox, VBox}

/**
 * Main for FDLog
 */
object FdCluster extends JFXApp with StructuredLogging {


  private val injector = Guice.createInjector(new Module(parameters))
  private val entryScene = injector.instance[EntryScene]
  private val dataScene = injector.instance[DataScene]
  private val clusterTab: ClusterTab = injector.instance[ClusterTab]
  private val statisticsTab = injector.instance[StatisticsTab]
  private val nodeAddress: NodeAddress = injector.instance[NodeAddress]
  private val runningTaskPane: RunningTaskPane = injector.instance[RunningTaskPane]
  private val statusPane: StatusPane = injector.instance[StatusPane]
  private val commandLine: CommandLine = injector.instance[CommandLine]
  private val allContestRules: AllContestRules = injector.instance[AllContestRules]
  private val contestProperty: ContestProperty = injector.instance[ContestProperty]
  try {
    injector.instance[Server]
  } catch {
    case e: Throwable ⇒
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

  private val fdclusterTabs: Seq[Tab] = Seq(entryTab, dataTab, clusterTab, statisticsTab)
  val tabPane: TabPane = new TabPane {
    tabs = fdclusterTabs
  }
  commandLine.getString("tab").foreach { tabText =>
val map: Map[String, Tab] = fdclusterTabs.map(t => t.text.value -> t).toMap
    val maybeTab = map.get(tabText)
    maybeTab.foreach((t: Tab) =>
      tabPane.selectionModel.value.select(t)
    )
  }

  contestProperty.logotypeImageProperty.onChange{(_,_,newImage: Image) =>
    imageView.image = newImage
  }
  private val imageView = new ImageView(contestProperty.logotypeImageProperty.value){
    styleClass += "contestLogo"
  }
  val bottomPane: GridPane = {

    new GridPane(){
      prefWidth = 400
      add(imageView, 0,0,1,2)
      add(runningTaskPane.pane, 1,0)
      add(statusPane.pane, 1,0)
    }
  }
  //  private val statsHeader = new HBox(Label(sorter"QSOs:  todo "))
  private val rootPane = new BorderPane {
    top = fdlogmenu.menuBar
    center = tabPane
    bottom = bottomPane  }
  val ourScene = new Scene()

  ourScene.getStylesheets.add(getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm)

  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm
  ourScene.getStylesheets.add(cssUrl)

  ourScene.root = rootPane

  stage = new PrimaryStage() {
    title = "FDCluster @ " + nodeAddress.display
    scene = ourScene
    private val externalForm: String = getClass.getResource("/images/FieldDay.png").toExternalForm
    icons += new Image(externalForm)
    onCloseRequest = { event =>
      Platform.exit()
      System.exit(0)

    }
  }
}
