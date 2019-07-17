
package org.wa9nnn.fdlog.javafx

import java.net.URL

import javafx.fxml.FXMLLoader
import javafx.{fxml ⇒ jfxf, scene ⇒ jfxs}
import org.wa9nnn.fdlog.model
import org.wa9nnn.fdlog.model.{NodeInfo, NodeInfoImpl, StationContext}
import org.wa9nnn.fdlog.store.StoreMapImpl
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object FdLog extends JFXApp {

  val resource: URL = getClass.getResource("/org/wa9nnn/fdlog/javafx/FDEntry.fxml")
  //    val root = FXMLLoader.load(resource)

  //  private val loader = new jfxf.FXMLLoader
  //  private val controller: FDLogEntryController = new FDLogEntryController
  //  loader.setController(controller)
  //  loader.setLocation(resource)

//  private val loader: FXMLLoader = new jfxf.FXMLLoader()
//  loader.setLocation(resource)
//  val root: jfxs.Parent = loader.load()
//
//  private val controller = loader.getController[FDLogEntryController]

  private val contest = model.Contest("WFD", 2019)
  implicit val nodeInfo: NodeInfo = new NodeInfoImpl(contest)

  private val store = new StoreMapImpl(nodeInfo)
  private val entryScene = new FDLogEntryScene()
  //  controller.init()
  //  val root: Parent = loader.load[jfxs.Parent]
  //  private val controller: FDLogEntryController = loader.getController[FDLogEntryController]

  //  println(root)

  private val stationContext = StationContext(store = store)

  new FDLogEntryController(entryScene)
  private val ourScene: Scene = entryScene.scene
  ourScene.getStylesheets.add()

  stage = new PrimaryStage() {
    title = "FXML GridPane Demo"
    scene = ourScene
  }


  //  controller.idQsoCallsign.setText("WA9NNN")
}