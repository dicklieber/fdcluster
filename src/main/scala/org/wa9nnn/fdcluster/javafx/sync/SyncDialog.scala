
package org.wa9nnn.fdcluster.javafx.sync

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.google.inject.name.Named
import javafx.scene.control
import javax.inject.Inject
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{ButtonType, Dialog, TableColumn, TableView}
import scalafx.stage.Modality

class SyncDialog @Inject()(@Named("stepsData") stepsData: ObservableBuffer[ProgressStep]) extends Dialog {
  title = "Sync Operation"
  val instantFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("mm:ss.SSS")

      .withLocale(Locale.US)
      .withZone(ZoneId.systemDefault());

  implicit def formatInstant(ldt: Instant): String = {
    instantFormatter.format(ldt)
  }

  val dp: control.DialogPane = dialogPane()
  dp.getButtonTypes.addAll(ButtonType.Close)
  initModality(Modality.None)

  private val tableView = new TableView(stepsData) {
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
