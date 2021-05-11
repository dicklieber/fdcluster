
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

package org.wa9nnn.fdcluster.javafx.data

import _root_.scalafx.Includes._
import _root_.scalafx.beans.property.{ReadOnlyObjectWrapper, ReadOnlyStringWrapper}
import _root_.scalafx.collections.ObservableBuffer
import _root_.scalafx.geometry.Pos
import _root_.scalafx.scene.Node
import _root_.scalafx.scene.control.TableColumn._
import _root_.scalafx.scene.control._
import _root_.scalafx.scene.layout.{HBox, VBox}
import akka.util.Timeout
import com.google.inject.Inject
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.contest.{Journal, JournalProperty}
import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.QsoRecord
import org.wa9nnn.fdcluster.store.StoreLogic
import org.wa9nnn.util.TimeHelpers
import play.api.libs.json.Json

import java.awt.Desktop
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZonedDateTime}
import java.util.concurrent.TimeUnit
/**
 * Create JavaFX UI to view QSOs.
 */
class DataScene @Inject()(journalProperty: JournalProperty, storeLogic: StoreLogic) {

  implicit val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  def format(instant: Instant): String = {
    formatter.format(ZonedDateTime.ofInstant(instant, TimeHelpers.utcZoneId))
  }

  //  private val allQsoBuffer: ObservableBuffer[QsoRecord] = StoreMapImpl.allQsos
  private val sizeLabel = new Label("--")
  private val qsoBuffer: ObservableBuffer[QsoRecord] = storeLogic.qsoBuffer
  sizeLabel.text = f"${qsoBuffer.size}%,d"
  qsoBuffer.onChange((ob, _) ⇒
    onFX {
      sizeLabel.text = f"${ob.size}%,d"
    }
  )

  var tableView: TableView[QsoRecord] = new TableView[QsoRecord](qsoBuffer) {
    columns ++= List(
      new TableColumn[QsoRecord, Instant] {
        text = "Stamp"
        tooltip = "Times in UTC"
        cellFactory = { _: TableColumn[QsoRecord, Instant] ⇒
          new TableCell[QsoRecord, Instant]() {
            styleClass += "dateTime"
            item.onChange { (_, oldValue, newValue) => {

              val maybeTime: Option[Instant] = Option(newValue).orElse(Some(oldValue))
              text = format(maybeTime.get)
            }
            }
          }
        }
        cellValueFactory = { q =>
          val ldt: Instant = q.value.qso.stamp
          val wrapper = ReadOnlyObjectWrapper(ldt)
          wrapper
        }
        prefWidth = 150
      },
      new TableColumn[QsoRecord, String] {
        text = "CallSign"
        cellFactory = { _: TableColumn[QsoRecord, String] ⇒
          new TableCell[QsoRecord, String]() {
            styleClass += "dateTime"
            item.onChange { (_, _, newValue) => {
              text = newValue
            }
            }
          }
        }

        cellValueFactory = { q =>
          val wrapper = ReadOnlyStringWrapper(q.value.qso.callSign)
          wrapper
        }
        prefWidth = 75
      },
      new TableColumn[QsoRecord, String] {
        text = "Band"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.qso.bandMode.bandName)
        }
        prefWidth = 50
      },
      new TableColumn[QsoRecord, String] {
        text = "Mode"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.qso.bandMode.modeName)
        }
        prefWidth = 50
      },
      new TableColumn[QsoRecord, String] {
        text = "Class"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.qso.exchange.entryClass)
        }
        prefWidth = 50
      },
      new TableColumn[QsoRecord, String] {
        text = "Section"
        cellValueFactory = { q =>
          val sectionCode: String = q.value.qso.exchange.sectionCode
          val name: String = {
            try {
              Sections.byCode(sectionCode).name
            } catch {
              case _: Exception =>
                "?"
            }
          }
          val display: String = sectionCode + " " + name
          ReadOnlyStringWrapper(display)
        }
        prefWidth = 150
      }
    )
  }

  private val selectionModel = tableView.selectionModel
  selectionModel.apply.selectedItem.onChange { (_, _, selectedQso) ⇒

    if (selectedQso != null) {
      val sJson = Json.prettyPrint(Json.toJson(selectedQso))
      detailView.setText(sJson)
    } else {
      detailView.setText("")
    }
  }
  tableView.setPrefWidth(400)
  val detailView = new TextArea()
  detailView.prefColumnCount = 30
  detailView.setMinWidth(250)
  private val splitPane = new SplitPane
  splitPane.items.addAll(tableView, detailView)
  splitPane.setDividerPosition(0, 50.0)
  private val desktop: Desktop = Desktop.getDesktop
  private val journalFileLabel = new Hyperlink(journalProperty.fileName) {
    onAction = event => {
      desktop.open(journalProperty.filePath.toFile)
    }
  }
  journalProperty.onChange{(_,_,newFile: Journal) =>
    journalFileLabel.text = journalProperty.fileName
  }


  val hbox: HBox = new HBox(
    new Label("QSO Journal file"),
    journalFileLabel,
    new Label("  QSO Count: "),
    sizeLabel
  )
  hbox.setAlignment(Pos.Center)
  hbox.getStyleClass.add("parenthetic")
  val vbox = new VBox(hbox, splitPane)
  val pane: Node = vbox


}
