
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

import _root_.scalafx.application.Platform
import _root_.scalafx.scene.Scene
import _root_.scalafx.scene.control.{Tab, TabPane}
import _root_.scalafx.scene.image.{Image, ImageView}
import _root_.scalafx.scene.layout.{BorderPane, GridPane}
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{Guice, Injector}
import com.sandinh.akuice.ActorInject
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.macos.DockIcon
import javafx.application.Application
import javafx.stage.Stage
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.http.Server
import org.wa9nnn.fdcluster.javafx.cluster.ClusterTab
import org.wa9nnn.fdcluster.javafx.data.DataTab
import org.wa9nnn.fdcluster.javafx.entry.{EntryTab, RunningTaskPane, StatisticsTab}
import org.wa9nnn.fdcluster.javafx.menu.FdClusterMenu
import org.wa9nnn.fdcluster.model.sync.ClusterActor
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty, NodeAddress}
import org.wa9nnn.fdcluster.store.network.BroadcastListener
import org.wa9nnn.fdcluster.{Module, NetworkPane}
import scalafx.Includes._
//import scalafx.stage.Stage

import java.awt.Desktop
import java.lang.management.ManagementFactory
import scala.util.{Failure, Success, Using}


object FdCluster extends App {
  //  def main(args: Array[String]): Unit = {
  println("at org.wa9nnn.fdcluster.javafx.FdCluster")
  Application.launch(classOf[org.wa9nnn.fdcluster.javafx.FdCluster1], args: _*)
  //  }
}

/**
 * Main for FDLog
 */
class FdCluster1 extends Application with LazyLogging with ActorInject {
  println(s"JAVA_HOME: \t${System.getenv("JAVA_HOME")}")
  println(s"java.home \t${System.getProperty("java.home")}")
  println(s"Java Version: \t${ManagementFactory.getRuntimeMXBean.getVmVersion}")
  println(s"Java VmName: \t${ManagementFactory.getRuntimeMXBean.getVmName}")
  println(s"Java VmVendor: \t${ManagementFactory.getRuntimeMXBean.getVmVendor}")
  val injector: Injector = Guice.createInjector(new Module())

  override def start(stage: Stage): Unit = {


    implicit val actorSystem = injector.instance[ActorSystem]
    // top level actors
//    val clusterActor: ActorRef = injectTopActor[ClusterActor]("clusterActor")
    val broqadcastListener: ActorRef = injectTopActor[BroadcastListener]("broadcastListener")


    val dataTab = injector.instance[DataTab]
    val entryTab = injector.instance[EntryTab]
    val clusterTab: ClusterTab = injector.instance[ClusterTab]
    val statisticsTab = injector.instance[StatisticsTab]
    val nodeAddress: NodeAddress = injector.instance[NodeAddress]
    val runningTaskPane: RunningTaskPane = injector.instance[RunningTaskPane]
    val statusPane: StatusPane = injector.instance[StatusPane]
    //    val commandLine: CommandLine = injector.instance[CommandLine]
    val contestProperty: ContestProperty = injector.instance[ContestProperty]
    val contestStatusPane: ContestStatusPane = injector.instance[ContestStatusPane]
    val allContestRules: AllContestRules = injector.instance[AllContestRules]
    try {
      injector.instance[Server]
    } catch {
      case e: Throwable ⇒
        e.printStackTrace()
    }
    val fdMenu: FdClusterMenu = injector.instance[FdClusterMenu]

    val fdclusterTabs: Seq[Tab] = Seq(entryTab, dataTab, clusterTab, statisticsTab)
    val tabPane: TabPane = new TabPane {
      tabs = fdclusterTabs
    }
    //    commandLine.getString("tab").foreach { tabText =>
    //      val map: Map[String, Tab] = fdclusterTabs.map(t => t.text.value -> t).toMap
    //      val maybeTab = map.get(tabText)
    //      maybeTab.foreach((t: Tab) =>
    //        tabPane.selectionModel.value.select(t)
    //      )
    //    }

    val imageView = new ImageView() {
      styleClass += "contestLogo"
    }
    val bottomPane: GridPane = {

      new GridPane() {
        prefWidth = 400
        add(imageView, 0, 0, 1, 3)
        add(runningTaskPane.pane, 1, 0)
        add(statusPane.pane, 1, 0)
        add(statusPane.pane, 1, 0)
        add(contestStatusPane, 1, 1)
      }
    }
    //  private val statsHeader = new HBox(Label(sorter"QSOs:  todo "))
    val rootPane = new BorderPane {
      top = fdMenu.menuBar
      center = tabPane
      bottom = bottomPane
      right = injector.instance[NetworkPane]
    }
    val ourScene: Scene = new Scene()

    val delegate: Any = ourScene.delegate

    //  private val url: URL = getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css")
    //  private val value: List[String] = List(url.toExternalForm)
    //  ourScene.stylesheets  = value

    val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm
    ourScene.stylesheets += cssUrl

    ourScene.root = rootPane


    //  stage = new PrimaryStage() {
    stage.title = "FDCluster @ " + nodeAddress.displayWithIp
    stage.scene = ourScene
    val externalForm: String = getClass.getResource("/images/FieldDay.png").toExternalForm
    stage.icons += new Image(externalForm)
    stage.onCloseRequest = {
      _ =>
        Platform.exit()
        System.exit(0)

    }
    //  }
    val desktop: Desktop = Desktop.getDesktop

    imageView.onMouseClicked = { e =>
      allContestRules.byContestName(contestProperty.contestName)
        .uri
        .foreach { uri =>
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
      onFX {
        stage.show()
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
}
