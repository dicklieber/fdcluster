package org.wa9nnn.fdlog.javafx.entry

import akka.actor.ActorRef
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.wa9nnn.fdlog.javafx.sync.{Step, SyncDialog}
import org.wa9nnn.fdlog.store.{DebugClearStore, Sync}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control._

import scala.collection.JavaConverters._
import org.wa9nnn.fdlog.javafx.sync.StepsDataMethod.addStep
import scalafx.collections.ObservableBuffer

class FdLogMenu @Inject()(stationDialog: StationDialog,
                          @Named("store") store: ActorRef,
                          @Named("stepsData")stepsData: ObservableBuffer[Step],
                          syncDialog: SyncDialog) extends LazyLogging {
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
      stepsData.step("Start", "Request")
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