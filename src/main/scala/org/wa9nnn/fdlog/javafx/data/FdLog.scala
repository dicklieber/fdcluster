
package org.wa9nnn.fdlog.javafx.data

import org.wa9nnn.fdlog.javafx.{FDLogEntryController, FDLogEntryScene}
import org.wa9nnn.fdlog.model
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.store.StoreMapImpl
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object FdLog extends JFXApp {


  private val contest = model.Contest("WFD", 2019)
  implicit val nodeInfo: NodeInfo = new NodeInfoImpl(contest)

  private val store = new StoreMapImpl(nodeInfo)

  private val stationContext = StationContext(
    store = store,
    operator = OurStation("WA9NNN", "IC-7300", "Endfed"),
    bandMode = BandMode(Band("20m"), Mode.phone))


  private val entryScene = new DataScene(stationContext).scene

  //  new FDLogEntryController(entryScene, stationContext)
  //  private val ourScene: Scene = entryScene.scene

  //  private val cssUrl: String = getClass.getResource("/org/wa9nnn/fdlog/javafx/fdlog.css").toExternalForm
  //  ourScene.getStylesheets.add(cssUrl)


  stage = new PrimaryStage() {
    title = "FDLog"
    scene = entryScene
  }


}
