package org.wa9nnn.fdlog.javafx.entry

import akka.actor.ActorRef
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.wa9nnn.fdlog.store.{DebugClearStore, Sync}
import org.wa9nnn.fdlog.store.network.cluster.FetchQsos
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control._

import scala.collection.JavaConverters._
import scala.concurrent.Future
import org.wa9nnn.fdlog.Markers.syncMarker
import org.wa9nnn.fdlog.javafx.sync.{StepsData, SyncDialog}
import scalafx.application.Platform

import scala.compat.Platform

class FdLogMenu @Inject()(stationDialog: StationDialog, @Named("store") store: ActorRef, stepsData: StepsData, syncDialog: SyncDialog) extends LazyLogging {
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
      d.dialogPane().buttonTypes = Seq(ButtonType.Close)
      d.showAndWait()
    }
  }
  private val currentStationMenuItem = new MenuItem {
    text = "Current Station"
    onAction = { _: ActionEvent =>
      try {
        stationDialog.apply()
      } catch {
        case eT: Throwable ⇒
          logger.error("Current Station", eT)
      }
    }
  }
  private val syncNowMenuItem = new MenuItem {
    text = "Sync with other nodes"
    onAction = { _: ActionEvent =>
      stepsData.clear()
      stepsData.step("Start", "Requeat")
      scalafx.application.Platform.runLater {

        syncDialog.showAndWait()
      }
      store ! Sync
    }
  }
  private val debugClearStoreMenuItem = new MenuItem {
    text = "Clear QSOs on this node"
    onAction = { _: ActionEvent =>
      store ! DebugClearStore
    }
  }
  //  TextInputDialog
  val menuBar: MenuBar = new MenuBar {
    menus = List(
      new Menu("_Debug") {
        mnemonicParsing = true
        items = List(
          debugClearStoreMenuItem
        )
      },
      new Menu("_Edit") {
        mnemonicParsing = true
        items = List(
          currentStationMenuItem,
          syncNowMenuItem,
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