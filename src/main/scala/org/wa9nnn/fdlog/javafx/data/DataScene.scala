
package org.wa9nnn.fdlog.javafx.data

import java.time.{LocalDateTime, ZoneOffset}

import com.google.inject.Inject
import org.wa9nnn.fdlog.javafx.Sections
import org.wa9nnn.fdlog.model.QsoRecord
import org.wa9nnn.fdlog.store.Store
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{TableColumn, TableView}

/**
 * Create JavaFX UI to view data base.
 */
class DataScene @Inject()(@Inject() store: Store) {
  def refresh(): Unit = {
    data.clear()
    data.addAll(ObservableBuffer[QsoRecord](store.dump))
  }


  import java.time.format.DateTimeFormatter

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  private var data: ObservableBuffer[QsoRecord] = ObservableBuffer[QsoRecord](store.dump)


  var tableView: TableView[QsoRecord] = new TableView[QsoRecord](data) {
    columns ++= List(
      new TableColumn[QsoRecord, String] {
        text = "Stamp"
        cellValueFactory = { q =>
          val ldt = LocalDateTime.ofInstant(q.value.qso.stamp, ZoneOffset.UTC)
          val s = ldt.format(formatter)
          val wrapper = ReadOnlyStringWrapper(s)
          wrapper
        }
        prefWidth = 150
      },
      new TableColumn[QsoRecord, String] {
        text = "Callsign"
        cellValueFactory = { q =>
          val wrapper = ReadOnlyStringWrapper(q.value.qso.callsign)
          wrapper
        }
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
          val name = Sections.find(section).foldLeft("") { (accum, section) ⇒ accum + section.name }

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
