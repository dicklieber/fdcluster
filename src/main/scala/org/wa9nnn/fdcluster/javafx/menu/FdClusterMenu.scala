package org.wa9nnn.fdcluster.javafx.menu

import akka.actor.ActorRef
import akka.util.Timeout
import com.google.inject.Injector
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import nl.grons.metrics4.scala.ByName.apply
import org.wa9nnn.fdcluster.javafx.debug.DebugRemoveDialog
import org.wa9nnn.fdcluster.javafx.sync.{SyncDialog, SyncSteps}
import org.wa9nnn.fdcluster.rig.{RigDialog, RigSettings}
import org.wa9nnn.fdcluster.store.{DebugClearStore, Sync}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control._

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.language.postfixOps
import net.codingwell.scalaguice.InjectorExtensions._

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
  private val debugRandomKillerMenuItem = new MenuItem {
    text = "Remove random QSOs"
    onAction = { _: ActionEvent =>
      debugRemoveDialog()
    }
  }
  private val aboutMenuItem = new MenuItem {
    text = "About"
    onAction = { _: ActionEvent =>
      AboutDialog()
    }
  }
  private val rigMenuItem = new MenuItem {
    text = "Rig"
    onAction = { _: ActionEvent =>
      injector.instance[RigDialog].showAndWait()
    }
  }
  val menuBar: MenuBar = new MenuBar {
    menus = List(
      new Menu("_File") {
        mnemonicParsing = true
        items = List(
          aboutMenuItem,
          rigMenuItem,
        )
      }, new Menu("_Debug") {
        mnemonicParsing = true
        items = List(
          debugClearStoreMenuItem,
          debugRandomKillerMenuItem
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