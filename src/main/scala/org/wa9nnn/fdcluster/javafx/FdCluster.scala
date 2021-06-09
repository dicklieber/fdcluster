
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

import _root_.scalafx.application.JFXApp.PrimaryStage
import _root_.scalafx.application.{JFXApp, Platform}
import _root_.scalafx.scene.Scene
import _root_.scalafx.scene.control.{Tab, TabPane}
import _root_.scalafx.scene.image.{Image, ImageView}
import _root_.scalafx.scene.layout.{BorderPane, GridPane}
import com.google.inject.Guice
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.macos.DockIcon
import net.codingwell.scalaguice.InjectorExtensions._
import org.wa9nnn.fdcluster.http.Server
import org.wa9nnn.fdcluster.javafx.cluster.ClusterTab
import org.wa9nnn.fdcluster.javafx.data.DataTab
import org.wa9nnn.fdcluster.javafx.entry.{EntryTab, RunningTaskPane, StatisticsTab}
import org.wa9nnn.fdcluster.javafx.menu.FdClusterMenu
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty, NodeAddress}
import org.wa9nnn.fdcluster.{Module, NetworkPane}
import org.wa9nnn.util.CommandLine

import java.awt.Desktop
import java.lang.management.ManagementFactory
import scala.util.{Failure, Success, Using}

/**
 * Main for FDLog
 */
object FdCluster extends JFXApp with LazyLogging {
  println(s"JAVA_HOME: \t${System.getenv("JAVA_HOME")}")
  println(s"java.home \t${System.getProperty("java.home")}")
  println(s"Java Version: \t${ManagementFactory.getRuntimeMXBean.getVmVersion}")
  println(s"Java VmName: \t${ManagementFactory.getRuntimeMXBean.getVmName}")
  println(s"Java VmVendor: \t${ManagementFactory.getRuntimeMXBean.getVmVendor}")


  private val injector = Guice.createInjector(new Module(parameters))
  private val entryTab = injector.instance[EntryTab]
  private val dataTab = injector.instance[DataTab]
  private val clusterTab: ClusterTab = injector.instance[ClusterTab]
  private val statisticsTab = injector.instance[StatisticsTab]
  private val nodeAddress: NodeAddress = injector.instance[NodeAddress]
  private val runningTaskPane: RunningTaskPane = injector.instance[RunningTaskPane]
  private val statusPane: StatusPane = injector.instance[StatusPane]
  private val commandLine: CommandLine = injector.instance[CommandLine]
  private val contestProperty: ContestProperty = injector.instance[ContestProperty]
  private val contestStatusPane: ContestStatusPane = injector.instance[ContestStatusPane]
  private val allContestRules: AllContestRules = injector.instance[AllContestRules]
  try {
    injector.instance[Server]
  } catch {
    case e: Throwable ⇒
      e.printStackTrace()
  }
  val fdMenu: FdClusterMenu = injector.instance[FdClusterMenu]

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

  private val imageView = new ImageView() {
    styleClass += "contestLogo"
  }
  val bottomPane: GridPane = {

    new GridPane() {
      prefWidth = 400
      add(imageView, 0, 0, 1, 3)
      add(runningTaskPane.pane, 1, 0)
      add(statusPane.pane, 1, 0)
      add(statusPane.pane, 1, 0)
      add(contestStatusPane, 1,1)
    }
  }
  //  private val statsHeader = new HBox(Label(sorter"QSOs:  todo "))
  private val rootPane = new BorderPane {
    top = fdMenu.menuBar
    center = tabPane
    bottom = bottomPane
    right = injector.instance[NetworkPane]
  }
  val ourScene = new Scene()

  ourScene.getStylesheets.add(getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm)

  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm
  ourScene.getStylesheets.add(cssUrl)

  ourScene.root = rootPane



  stage = new PrimaryStage() {
    title = "FDCluster @ " + nodeAddress.displayWithIp
    scene = ourScene
    private val externalForm: String = getClass.getResource("/images/FieldDay.png").toExternalForm
    icons += new Image(externalForm)
    onCloseRequest = {
      _ =>
        Platform.exit()
        System.exit(0)

    }
  }
  val desktop: Desktop = Desktop.getDesktop

  imageView.onMouseClicked = { e =>
    allContestRules.byContestName(contestProperty.contestName)
      .uri
      .foreach{ uri =>
      desktop.browse(uri)
    }

  }

  // This can hang, calling com.apple.eawt.Application
  // if invoked too early.
  setUpImage(contestProperty.contestName)

  contestProperty.onChange { (_, _, nv) =>
    setUpImage(nv.contestName)
  }

  def setUpImage(contestName: String): Unit = {
    val imagePath: String = s"/images/$contestName.png"
    Using(getClass.getResourceAsStream(imagePath)) { is =>
      new Image(is, 150.0, 150.0, true, true)
    } match {
      case Failure(exception) =>
        logger.error(s"loading: $imagePath", exception)
      case Success(image) =>
        imageView.image = image
    }

    try {
      {
        DockIcon(imagePath)
      }
    } catch {
      case e: java.lang.NoClassDefFoundError =>
        logger.debug("Icon switch", e)
      case et: Throwable =>
        logger.debug("Icon switch", et)
    }
  }

}
