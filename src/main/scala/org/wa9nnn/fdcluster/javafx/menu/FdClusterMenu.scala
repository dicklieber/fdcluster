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
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.wa9nnn.fdcluster.cabrillo.{CabrilloDialog, CabrilloExportRequest}
import org.wa9nnn.fdcluster.contest.fieldday.{SummaryEngine, WinterFieldDaySettings}
import org.wa9nnn.fdcluster.dupsheet.GenerateDupSheet
import org.wa9nnn.fdcluster.http.Sendable
import org.wa9nnn.fdcluster.javafx.debug.DebugRemoveDialog
import org.wa9nnn.fdcluster.javafx.sync.{RequestUuidsForHour, SyncDialog, SyncSteps}
import org.wa9nnn.fdcluster.metrics.MetricsReporter
import org.wa9nnn.fdcluster.model.{ContestProperty, ExportFile, NodeAddress}
import org.wa9nnn.fdcluster.rig.RigDialog
import org.wa9nnn.fdcluster.store.{DebugClearStore, Sync}
import org.wa9nnn.fdcluster.tools.RandomQsoDialog
import org.wa9nnn.fdcluster.{FileManager, QsoCountCollector}
import org.wa9nnn.util.StructuredLogging
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control._
import org.wa9nnn.fdcluster.model.MessageFormats._
import java.awt.Desktop
import java.io.{PrintWriter, StringWriter}
import java.nio.file.Files
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try, Using}

class FdClusterMenu @Inject()(implicit
                              injector: Injector,
                              @Named("store") store: ActorRef,
                              syncSteps: SyncSteps,
                              syncDialog: SyncDialog,
                              aboutDialog: AboutDialog,
                              nodeAddress: NodeAddress,
                              fileManager: FileManager,
                              generateDupSheet: GenerateDupSheet,
                              contestProperty: ContestProperty,
                              summaryEngine: SummaryEngine,
                              metricsReporter: MetricsReporter,
                              debugRemoveDialog: DebugRemoveDialog) extends StructuredLogging {
  private implicit val timeout = Timeout(5 seconds)
  private val desktop = Desktop.getDesktop
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
  private val requestUuidForHour = new MenuItem {
    text = "RequestUuidsForHour"
    onAction = { _: ActionEvent =>
      store ! Sendable(RequestUuidsForHour(), nodeAddress.uri)
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
      aboutDialog()
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
      val cabrilloDialog: CabrilloDialog = injector.instance[CabrilloDialog]
      cabrilloDialog.showAndWait() match {
        case Some(cabrilloExportRequest: CabrilloExportRequest) =>
          store ! cabrilloExportRequest
        case _ =>
      }
    }
  }
  private val fieldDaySummary = new MenuItem {
    text = "FieldDay Entry Summary"
    onAction = { _ =>
      val writer = new StringWriter

      val wfd = WinterFieldDaySettings()
      summaryEngine(writer, contestProperty.contest, wfd)
      writer.close()

      fileManager.defaultExportFile(".html")
      val path = Files.createTempFile("SummaryEngineSpec", ".html")
      Files.writeString(path, writer.toString)
      val uri = path.toUri
      Desktop.getDesktop.browse(uri)
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

  private val metricsMenuItem = new MenuItem{
    text = "Metrics"
    onAction = {_ =>
      metricsReporter.report()
    }
  }

  private val dupSheetMenuItem = new MenuItem {
    text = "Dup Sheet"
    onAction = { _ =>

      val dupFile: ExportFile = fileManager.defaultExportFile("dup")(contestProperty)
      val r: Try[Int] = Using(new PrintWriter(Files.newBufferedWriter(dupFile.path))) { pw =>
        generateDupSheet(pw)
      }
      r match {
        case Failure(exception) =>
          logger.error("Generating Dup", exception)
        case Success(qsoCount) =>
          Desktop.getDesktop.open(dupFile.path.toFile)
          logJson("Dup Write")
            .++("path" -> dupFile.path)
            .++("qsoCount" -> qsoCount)
            .info()
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
          rigMenuItem,
          importMenuItem,
          exportMenuItem,
          exportCabrillo,
          new SeparatorMenuItem(),
          dupSheetMenuItem,
          fieldDaySummary,
          new SeparatorMenuItem(),
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
          generateTimed,
          metricsMenuItem,
          requestUuidForHour
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
          //          environmentMenuItem,
          aboutMenuItem,
        )
      }
    )
  }
}