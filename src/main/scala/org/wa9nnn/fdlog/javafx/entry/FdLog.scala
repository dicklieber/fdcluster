
package org.wa9nnn.fdlog.javafx.entry

import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._
import org.wa9nnn.fdlog.javafx.data.DataScene
import org.wa9nnn.fdlog.model.{NodeInfo, NodeInfoImpl}
import org.wa9nnn.fdlog.{Module, model}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Tab, TabPane}

object FdLog extends JFXApp {

  private val injector = Guice.createInjector(new Module())
  private val contest = model.Contest("WFD", 2019)
  implicit val nodeInfo: NodeInfo = new NodeInfoImpl(contest)

  private val dataScene = injector.instance[DataScene]
  private val entryScene = injector.instance[FDLogEntryScene]


  val tabPane: TabPane = new TabPane {
    tabs = Seq(
      new Tab {
        text = "Entry"
        content = entryScene.pane
      },
      new Tab {
        text = "Data"
        content = dataScene.tableView
      }
    )
  }
  val ourScene = new Scene()

  ourScene.getStylesheets.add(getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm)

  private val cssUrl: String = getClass.getResource("/fdlog.css").toExternalForm
  ourScene.getStylesheets.add(cssUrl)

  ourScene.root = tabPane

  stage = new PrimaryStage() {
    title = "FDLog"
    scene = ourScene
  }

}
