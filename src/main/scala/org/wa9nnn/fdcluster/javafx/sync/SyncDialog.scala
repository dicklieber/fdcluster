
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

package org.wa9nnn.fdcluster.javafx.sync

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale

import javafx.scene.control
import javax.inject.Inject
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{ButtonType, Dialog, TableColumn, TableView}
import scalafx.stage.Modality

class SyncDialog @Inject()(syncSteps: SyncSteps) extends Dialog {
  title = "Sync Operation"
  val instantFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("mm:ss.SSS")
      .withLocale(Locale.US)
      .withZone(ZoneId.systemDefault())

  implicit def formatInstant(ldt: Instant): String = {
    instantFormatter.format(ldt)
  }

  val dp: control.DialogPane = dialogPane()
  dp.getButtonTypes.addAll(ButtonType.Close)
  initModality(Modality.None)

  private val tableView = new TableView(syncSteps.observableBuffer) {
    columns ++= List(
      new TableColumn[ProgressStep, String] {
        text = "Start"
        cellValueFactory = { q =>
          val step = q.value
          val wrapper = ReadOnlyStringWrapper(step.start)
          wrapper
        }
        prefWidth = 75
      },
      new TableColumn[ProgressStep, String] {
        text = "ProgressStep"
        cellValueFactory = { q =>
          val wrapper = ReadOnlyStringWrapper(q.value.name)
          wrapper
        }
        prefWidth = 150
      },
      new TableColumn[ProgressStep, String] {
        text = "Result"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.result)
        }
        prefWidth = 200
      }
    )
  }
  dp.setContent(tableView)

}
