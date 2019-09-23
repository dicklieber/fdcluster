package org.wa9nnn.fdlog.javafx.entry

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control._

import scala.collection.JavaConverters._

class FdLogMenu @Inject()(stationDialog: StationDialog) extends LazyLogging{
  logger.debug("FdLogMenu")

  private val environmentMenuItem = new MenuItem {
    text = "Environment"
    onAction = { _: ActionEvent =>
      val d: Dialog[Nothing] = new Dialog() {
        title = "Information Dialog"
        private val keys = System.getProperties.keySet().asScala.map(_.toString)
        contentText =
          keys.toList
            .sorted
            .map(key ⇒
              s"$key: \t${System.getProperty(key).take(35)}").mkString("\n")
      }
      d.dialogPane().buttonTypes = Seq( ButtonType.Close)
      d.showAndWait()
    }
  }
  private val currentStationMeniuItem = new MenuItem {
    text = "Current Station"
    onAction = { _: ActionEvent =>
      try {
        stationDialog.apply
      } catch {
        case eT:Throwable ⇒
          logger.error("Current Station", eT)
      }
    }
  }
    //  TextInputDialog
    val menuBar: MenuBar = new MenuBar {
      menus = List(
        new Menu("_File") {
          mnemonicParsing = true
          items = List(
            new MenuItem("New..."),
            new MenuItem("Save")
          )
        },
        new Menu("_Edit") {
          mnemonicParsing = true
          items = List(
            currentStationMeniuItem,
            new MenuItem("Copy"),
            new MenuItem("Paste")
          )
        },
        new Menu("_Help") {
          mnemonicParsing = true
          items = List(
            environmentMenuItem,
            new MenuItem("About"),
          )
        }
      )
    }
}