package org.wa9nnn.fdcluster.javafx.menu

import akka.actor.ActorRef
import akka.util.Timeout
import com.google.inject.Injector
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.wa9nnn.fdcluster.javafx.debug.DebugRemoveDialog
import org.wa9nnn.fdcluster.javafx.sync.{SyncDialog, SyncSteps}
import org.wa9nnn.fdcluster.rig.RigDialog
import org.wa9nnn.fdcluster.store.{DebugClearStore, Sync}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control._

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

class FdClusterMenu @Inject()(
                               injector: Injector,
                               @Named("store") store: ActorRef,
                               syncSteps: SyncSteps,
                               syncDialog: SyncDialog,
                               debugRemoveDialog: DebugRemoveDialog) extends LazyLogging {
  private implicit val timeout = Timeout(5 seconds)

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
      injector.instance[StationDialog].showAndWait()
    }
  }
  private val syncNowMenuItem = new MenuItem {
    text = "Sync with other nodes"
    onAction = { _: ActionEvent =>
      syncSteps.start()
      syncSteps.step("Start", "Request")
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
  private val debugDemoBulkMenuItem = new MenuItem {
    text = "Add fake QSOs."
    onAction = { _: ActionEvent =>
      val dialog = injector.instance[BuildLoadDialog]
      dialog.showAndWait() match {
        case Some(blr) =>
          store ! blr
        case None =>
      }

    }
  }
  private val debugRandomKillerMenuItem = new MenuItem {
    text = "Remove random QSOs"
    onAction = { _: ActionEvent =>
      debugRemoveDialog()
    }
  }
  private val aboutMenuItem = new MenuItem {
    text = "_About"
    onAction = { _: ActionEvent =>
      AboutDialog()
    }
  }
  private val rigMenuItem = new MenuItem {
    text = "_Rig"
    onAction = { _: ActionEvent =>
      injector.instance[RigDialog].showAndWait()
    }
  }
  private val importMenuItem = new MenuItem {
    text = "_Import"
    onAction = { _: ActionEvent =>
      injector.instance[ImportDialog].showAndWait() match {
        case Some(importRequest) =>
          store ! importRequest
        case None =>
      }
    }
  }

  private val exitMenuItem = new MenuItem {
    text = "Exit"
    onAction = { _ =>
      Platform.exit()
      System.exit(0)
    }
  }

  val menuBar: MenuBar = new MenuBar {
    menus = List(
      new Menu("_File") {
        mnemonicParsing = true
        items = List(
          aboutMenuItem,
          rigMenuItem,
          importMenuItem,
          exitMenuItem,
        )
      }, new Menu("_Debug") {
        mnemonicParsing = true
        items = List(
          debugClearStoreMenuItem,
          debugRandomKillerMenuItem,
          debugDemoBulkMenuItem
        )
      },
      new Menu("_Edit") {
        mnemonicParsing = true
        items = List(
          currentStationMenuItem,
        )
      },
      new Menu("_Sync") {
        mnemonicParsing = true
        items = List(
          syncNowMenuItem,
        )
      },
      new Menu("_Help") {
        mnemonicParsing = true
        items = List(
          environmentMenuItem,
          //          aboutMenuItem,
        )
      }
    )
  }
}