
package org.wa9nnn.fdlog.javafx.entry

import java.net.URL

import org.wa9nnn.fdlog.javafx.data.DataScene
import org.wa9nnn.fdlog.javafx.{FDLogEntryController, FDLogEntryScene}
import org.wa9nnn.fdlog.model
import org.wa9nnn.fdlog.model.{Band, BandMode, NodeInfo, NodeInfoImpl, OurStation, StationContext}
import org.wa9nnn.fdlog.store.StoreMapImpl
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import org.wa9nnn.fdlog.model.Mode
import scalafx.scene.control.{SkinBase, Tab, TabPane}

object FdLog extends JFXApp {


  private val contest = model.Contest("WFD", 2019)
  implicit val nodeInfo: NodeInfo = new NodeInfoImpl(contest)

  private val store = new StoreMapImpl(nodeInfo)
  private val entryScene = new FDLogEntryScene()

  private val stationContext = StationContext(
    store = store,
    operator = OurStation("WA9NNN", "IC-7300", "Endfed"),
    bandMode = BandMode(Band("20m"), Mode.phone))


  new FDLogEntryController(entryScene, stationContext)
//   entryScene.scene

  private val dataScene = new DataScene(stationContext)


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

  private val modena: URL = getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css")
  println(s"modena: $modena")
  ourScene.getStylesheets.add(modena.toExternalForm)

  private val cssUrl: String = getClass.getResource("/fdlog.css").toExternalForm
  ourScene.getStylesheets.add(cssUrl)

  ourScene.root = tabPane

  stage = new PrimaryStage() {
    title = "FDLog"
    scene = ourScene
  }

//  override def main(args: Array[String]): Unit = {
//    super.main(args)
//  }

}
