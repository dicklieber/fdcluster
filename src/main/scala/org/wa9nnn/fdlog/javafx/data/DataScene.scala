
package org.wa9nnn.fdlog.javafx.data

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import org.wa9nnn.fdlog.javafx.entry.Sections
import org.wa9nnn.fdlog.model.QsoRecord
import play.api.libs.json.Json
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.{ReadOnlyObjectWrapper, ReadOnlyStringWrapper}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control.TableColumn._
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}

/**
 * Create JavaFX UI to view QSOs.
 */
class DataScene @Inject()(@Named("store") store: ActorRef,
                          @Named("journalPath") journalPath: Path,
                          @Named("allQsos") allQsoBuffer: ObservableBuffer[QsoRecord]) {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  //  def refresh(): Unit = {
  //    //todo  should not need, use [[org.wa9nnn.fdlog.store.StoreMapImpl.allQsos]]
  ////    val future = store ? DumpQsos
  ////    val qsos = Await.result(future, timeout.duration).asInstanceOf[Seq[QsoRecord]]
  ////
  ////    data.clear()
  ////    data.addAll(ObservableBuffer[QsoRecord](qsos))
  //  }

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")


//  private val allQsoBuffer: ObservableBuffer[QsoRecord] = StoreMapImpl.allQsos
  private val sizeLabel = new Label("--")

  allQsoBuffer.onChange((ob, _) ⇒
    Platform.runLater { // on scalafx thread
      sizeLabel.text = f"${ob.size}%,d"
    }
  )

  var tableView: TableView[QsoRecord] = new TableView[QsoRecord](allQsoBuffer) {
    columns ++= List(
      new TableColumn[QsoRecord, LocalDateTime] {
        text = "Stamp"
        cellFactory = { _ ⇒
          new TableCell[QsoRecord, LocalDateTime]() {
            styleClass += "dateTime"
            item.onChange { (ov, oldValue, newValue) => {

              val maybeTime = Option(newValue).orElse(Some(oldValue))
              text = formatter.format(maybeTime.get)
            }
            }
          }
        }
        cellValueFactory = { q =>
          val ldt = q.value.qso.stamp
          val s = ldt.format(formatter)
          val wrapper = ReadOnlyObjectWrapper(ldt)
          wrapper
        }
        prefWidth = 150
      },
      new TableColumn[QsoRecord, String] {
        text = "Callsign"
        cellFactory = { _ ⇒
          new TableCell[QsoRecord, String]() {
            styleClass += "dateTime"
            item.onChange { (ov, oldValue, newValue) => {
              text = newValue
            }
            }
          }
        }

        cellValueFactory = { q =>
          val wrapper = ReadOnlyStringWrapper(q.value.qso.callsign)
          wrapper
        }
        prefWidth = 75
      },
      new TableColumn[QsoRecord, String] {
        text = "Band"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.qso.bandMode.band)
        }
        prefWidth = 50
      },
      new TableColumn[QsoRecord, String] {
        text = "Mode"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.qso.bandMode.mode)
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

  private val selectionModel = tableView.selectionModel
  selectionModel.apply.selectedItem.onChange { (_, _, selectedQso) ⇒
    import org.wa9nnn.fdlog.model.MessageFormats._
    val sJson = Json.prettyPrint(Json.toJson(selectedQso))
    detailView.setText(sJson)
  }
  tableView.setPrefWidth(400)
  val detailView = new TextArea()
  detailView.prefColumnCount = 30
  detailView.setMinWidth(250)
  private val splitPane = new SplitPane
  splitPane.items.addAll(tableView, detailView)
  splitPane.setDividerPosition(0, 50.0)
  //  val journalFileLabel = new Label(s"QSO Journal file: ${journalPath.toAbsolutePath}")
  //  journalFileLabel.setAlignment(Pos.Center)
  //  journalFileLabel.getStyleClass.add("parenthetic");
  val hbox: HBox = new HBox(
    new Label("QSO Journal file"),
    new Label(journalPath.toAbsolutePath.toString),
    new Label("  QSO Count: "),
    sizeLabel
  )
  hbox.setAlignment(Pos.Center)
  hbox.getStyleClass.add("parenthetic")
  val vbox = new VBox(hbox, splitPane)
  val pane: Node = vbox


}
