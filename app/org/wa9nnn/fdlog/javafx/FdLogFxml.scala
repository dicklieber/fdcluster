
package org.wa9nnn.fdlog.javafx

import java.io.IOException
import java.net.URL

import javafx.fxml.FXMLLoader
import javafx.{fxml ⇒ jfxf}
import javafx.{scene ⇒ jfxs}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object FdLogFxml extends JFXApp {

    val resource: URL = getClass.getResource("/org/wa9nnn/fdlog/javafx/FDEntry.fxml")
//    val root = FXMLLoader.load(resource)

//  private val loader = new jfxf.FXMLLoader
//  private val controller: FDLogEntryController = new FDLogEntryController
//  loader.setController(controller)
//  loader.setLocation(resource)

  private val loader: FXMLLoader = new jfxf.FXMLLoader()
  loader.setLocation(resource)
  val root: jfxs.Parent = loader.load()

  private val controller: FDLogEntryController = loader.getController[org.wa9nnn.fdlog.javafx.FDLogEntryController]


//  val root: Parent = loader.load[jfxs.Parent]
//  private val controller: FDLogEntryController = loader.getController[FDLogEntryController]

//  println(root)
  private val scene1 = new Scene(root)

  stage = new PrimaryStage() {
      title = "FXML GridPane Demo"
      scene = scene1
    }

//  controller.idQsoCallsign.setText("WA9NNN")
  }