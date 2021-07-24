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

import _root_.scalafx.Includes._
import _root_.scalafx.application.Platform
import _root_.scalafx.event.ActionEvent
import _root_.scalafx.scene.control._
import akka.util.Timeout
import com.google.inject.Injector
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.wa9nnn.fdcluster.QsoCountCollector
import org.wa9nnn.fdcluster.cabrillo.{CabrilloDialog, CabrilloExportRequest}
import org.wa9nnn.fdcluster.contest.fieldday.{PostContestDialogFD, SummaryEngine}
import org.wa9nnn.fdcluster.contest.{ContestDialog, OkGate}
import org.wa9nnn.fdcluster.dupsheet.GenerateDupSheet
import org.wa9nnn.fdcluster.javafx.cluster.FdHoursDialog
import org.wa9nnn.fdcluster.javafx.debug.{DebugRemoveDialog, ResetDialog}
import org.wa9nnn.fdcluster.logging.LogStashDialog
import org.wa9nnn.fdcluster.metrics.MetricsReporter
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.rig.RigDialog
import org.wa9nnn.fdcluster.store.{ClearStore, StoreSender}
import org.wa9nnn.fdcluster.tools.RandomQsoDialog

import java.awt.Desktop
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class FdClusterMenu @Inject()(
                               injector: Injector,
                               store: StoreSender,
                               aboutDialog: AboutDialogGrid,
                               generateDupSheet: GenerateDupSheet,
                               summaryEngine: SummaryEngine,
                               metricsReporter: MetricsReporter,
                               nodeAddress: NodeAddress,
                               debugRemoveDialog: DebugRemoveDialog) extends LazyLogging {
  private implicit val timeout: Timeout = Timeout(5 seconds)
  private val desktop = Desktop.getDesktop

  private val webClient: MenuItem = new MenuItem {
    text = "Use Web Client"
    onAction = { _: ActionEvent =>
      desktop.browse(nodeAddress.url.toURI)
    }
  }

  private val contestSetupMenuItem: MenuItem = new MenuItem {
    text = "Pre Contest Setup"
    onAction = { _: ActionEvent =>
      injector.instance[ContestDialog].showAndWait()
    }
  }
  private val postContestMenuItem: MenuItem = new MenuItem {
    text = "Post Contest"
    onAction = { _: ActionEvent =>
      injector.instance[PostContestDialogFD].showAndWait()
    }
  }
  private val dumpStatsMenuItem = new MenuItem {
    text = "Dump Stats to Console"
    private val qsoStatCollector: QsoCountCollector = injector.instance[QsoCountCollector]
    onAction = { _: ActionEvent =>
      qsoStatCollector.dumpStats()
    }
  }

  private val debugClearStoreMenuItem = new MenuItem {
    text = "Clear QSOs on this node"
    onAction = { _: ActionEvent =>
      store ! ClearStore
    }
  }
  private val debugRandomKillerMenuItem = new MenuItem {
    text = "Remove random QSOs"
    onAction = { _: ActionEvent =>
      debugRemoveDialog()
    }
  }

  private val centralDataCollection = new MenuItem{
    text = "Central Data Collection"
    onAction = { _: ActionEvent =>
      injector.instance[LogStashDialog]
    }
  }

  private val aboutMenuItem = new MenuItem {
    text = "_About "
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
    text = "Import Adif"
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
    text = "Write Cabrillo"
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
      summaryEngine.invoke()
    }
  }

  private val exitMenuItem = new MenuItem {
    text = "Exit"
    onAction = { _ =>
      Platform.exit()
      System.exit(0)
    }
  }

  private val metricsMenuItem = new MenuItem {
    text = "Metrics"
    onAction = { _ =>
      metricsReporter.report()
    }
  }

  private val dupSheetMenuItem = new MenuItem {
    text = "Dup Sheet"
    onAction = { _ =>
      generateDupSheet.invoke()    }
  }


  private val generateTimed = new MenuItem {
    text = "Generate Timed"
    onAction = { _ =>
      new RandomQsoDialog().showAndWait().foreach { grq =>
        store ! grq
      }
    }
  }
  private val fdHours = new MenuItem {
    text = "FdHours Syncing"
    onAction = { _ =>
      injector.instance[FdHoursDialog].showAndWait()
    }
  }

  OkGate.onChange { (_, _, nv) =>
    disable(!nv)
  }

  disable(!OkGate.value)

  def disable(disable: Boolean): Unit = {
    generateTimed.disable = disable

  }


  private val upDown = new MenuItem {
    text = "Cluster"
    onAction = () =>
      injector.instance[ResetDialog].showAndWait()

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
          exitMenuItem,
        )
      }, new Menu("_Debug") {
        mnemonicParsing = true
        items = List(
          fdHours,
          centralDataCollection,
          upDown,
          dumpStatsMenuItem,
          debugClearStoreMenuItem,
          debugRandomKillerMenuItem,
          generateTimed,
          metricsMenuItem,
        )
      },
      new Menu("Contest") {
        mnemonicParsing = true
        items = List(
          contestSetupMenuItem,
          postContestMenuItem
        )
      },
      new Menu("Web") {
        mnemonicParsing = true
        items = List(
          webClient,
        )
      },
      new Menu("_Help") {
        mnemonicParsing = true
        items = List(
          //          environmentMenuItem,
          aboutMenuItem
        )
      }
    )
  }
  menuBar.useSystemMenuBar = true
}