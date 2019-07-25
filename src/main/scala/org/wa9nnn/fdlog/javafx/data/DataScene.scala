
package org.wa9nnn.fdlog.javafx.data

import java.time.{LocalDateTime, ZoneOffset}

import org.wa9nnn.fdlog.javafx.{Section, Sections}
import org.wa9nnn.fdlog.model.{QsoRecord, StationContext}
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{TableColumn, TableView}

/**
  * Create JavaFX UI to view data base.
  */
class DataScene(stationContext: StationContext) {

  import java.time.format.DateTimeFormatter

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  val data: ObservableBuffer[QsoRecord] = ObservableBuffer[QsoRecord](stationContext.store.dump)


   val tableView: TableView[QsoRecord] = new TableView[QsoRecord](data) {
    columns ++= List(
      new TableColumn[QsoRecord, String] {
        text = "Stamp"
        cellValueFactory = { q =>
          val ldt = LocalDateTime.ofInstant(q.value.qso.stamp, ZoneOffset.UTC)
          val s = ldt.format(formatter)
          val wrapper = ReadOnlyStringWrapper(s)
          wrapper }
        prefWidth = 150
      },
      new TableColumn[QsoRecord, String] {
        text = "Callsign"
        cellValueFactory = { q =>
          val wrapper = ReadOnlyStringWrapper(q.value.qso.callsign)
          wrapper }
        prefWidth = 75
      },
      new TableColumn[QsoRecord, String] {
        text = "Band"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.qso.bandMode.band.band)
        }
        prefWidth = 50
      },
      new TableColumn[QsoRecord, String] {
        text = "Mode"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.qso.bandMode.mode.name())
        }
        prefWidth = 50
      },
      new TableColumn[QsoRecord, String] {
        text = "Class"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.qso.exchange.category)
        }
        prefWidth = 50
      },
      new TableColumn[QsoRecord, String] {
        text = "Section"
        cellValueFactory = { q =>
          val section: String = q.value.qso.exchange.section
          val name = Sections.find(section).foldLeft(""){(accum, section) ⇒ accum + section.name}

          val display: String = section + " " + name
          ReadOnlyStringWrapper(display)
        }
        prefWidth = 150
      }
    )

  }

  val scene: Scene = new Scene {
    root = tableView
  }
}
