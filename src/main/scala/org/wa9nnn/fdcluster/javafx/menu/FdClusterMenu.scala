/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.javafx.menu

import akka.actor.ActorRef
import akka.util.Timeout
import com.google.inject.Injector
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.wa9nnn.fdcluster.cabrillo.{CabrilloDialog, CabrilloExportRequest}
import org.wa9nnn.fdcluster.dupsheet.GenerateDupSheet
import org.wa9nnn.fdcluster.javafx.debug.DebugRemoveDialog
import org.wa9nnn.fdcluster.javafx.sync.{SyncDialog, SyncSteps}
import org.wa9nnn.fdcluster.model.{ContestProperty, ExportFile}
import org.wa9nnn.fdcluster.rig.RigDialog
import org.wa9nnn.fdcluster.store.{DebugClearStore, Sync}
import org.wa9nnn.fdcluster.tools.RandomQsoDialog
import org.wa9nnn.fdcluster.{FileManager, QsoCountCollector}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control._

import java.awt.Desktop
import java.io.PrintWriter
import java.nio.file.Files
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try, Using}

class FdClusterMenu @Inject()(
                               injector: Injector,
                               @Named("store") store: ActorRef,
                               syncSteps: SyncSteps,
                               syncDialog: SyncDialog,
                               fileManager: FileManager,
                               generateDupSheet: GenerateDupSheet,
                               contestProperty: ContestProperty,
                               debugRemoveDialog: DebugRemoveDialog) extends LazyLogging {
  private implicit val timeout = Timeout(5 seconds)
  private val desktop = Desktop.getDesktop
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
      injector.instance[ContestDialog].showAndWait()
    }
  }
  private val dumpStatsMenuItem = new MenuItem {
    text = "Dump Stats to Console"
    private val qsoStatCollector: QsoCountCollector = injector.instance[QsoCountCollector]
    onAction = { _: ActionEvent =>
      qsoStatCollector.dumpStats()
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
  private val exportMenuItem = new MenuItem {
    text = "Export ADIF"
    onAction = { _: ActionEvent =>
      injector.instance[ExportDialog].showAndWait() match {
        case Some(exportRequest) =>
          store ! exportRequest
        case None =>
      }
    }
  }
  private val exportCabrillo = new MenuItem {
    text = "export Cabrillo"
    onAction = { _: ActionEvent =>
      val cg = injector.instance[CabrilloDialog]
      cg.showAndWait() match {
        case Some(cabrilloExportRequest: CabrilloExportRequest) =>
          store ! cabrilloExportRequest
        case None =>
      }
    }
  }
  private val filesMenuItem = new MenuItem {
    text = "FdCluster Files"
    onAction = { _ =>
      desktop.browseFileDirectory(fileManager.directory.toFile)
    }
  }

  private val exitMenuItem = new MenuItem {
    text = "Exit"
    onAction = { _ =>
      Platform.exit()
      System.exit(0)
    }
  }

  private val dupSheetMenuItem = new MenuItem {
    text = "Dup Sheet"
    onAction = { _ =>

      val dupFile: ExportFile = fileManager.defaultExportFile("dup")(contestProperty)
      val r: Try[Unit] = Using(new PrintWriter(Files.newBufferedWriter(dupFile.path))) { pw =>
        generateDupSheet(pw)
      }
      r match {
        case Failure(exception) =>
          logger.error("Generating Dup", exception)
        case Success(value) =>
          Desktop.getDesktop.open(dupFile.path.toFile)
      }
    }
  }

  private val generateTimed = new MenuItem {
    text = "Generate Timed"
    onAction = { _ =>
      new RandomQsoDialog().showAndWait().foreach { grq =>
        store ! grq
      }
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
          exportMenuItem,
          exportCabrillo,
          dupSheetMenuItem,
          filesMenuItem,
          exitMenuItem,
        )
      }, new Menu("_Debug") {
        mnemonicParsing = true
        items = List(
          dumpStatsMenuItem,
          debugClearStoreMenuItem,
          debugRandomKillerMenuItem,
          debugDemoBulkMenuItem,
          generateTimed
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