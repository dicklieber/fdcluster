
package org.wa9nnn.fdlog.javafx.sync

import javafx.scene.control.DialogPane
import javax.inject.Inject
import scalafx.beans.property.{ObjectProperty, ReadOnlyStringWrapper}
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{ButtonType, Dialog, TableColumn, TableView}

class SyncDialog @Inject()(stepsData: StepsData) extends Dialog {
  val dp: DialogPane = dialogPane()
  dp.getButtonTypes.addAll(ButtonType.Close)

  val tableView: TableView[Step] = new TableView[Step](stepsData) {
    columns ++= List(
      new TableColumn[Step, String] {
        text = "Start"
        cellValueFactory = { q =>
          val step = q.value
          val wrapper = ReadOnlyStringWrapper(step.start.toString)
          wrapper
        }
        prefWidth = 150
      },
      new TableColumn[Step, String] {
        text = "Step"
        cellValueFactory = { q =>
          val wrapper = ReadOnlyStringWrapper(q.value.name)
          wrapper
        }
        prefWidth = 75
      },
      new TableColumn[Step, String] {
        text = "Result"
        cellValueFactory = { q =>
          ReadOnlyStringWrapper(q.value.result)
        }
        prefWidth = 50
      }
    )
  }
  dp.setContent(tableView)

}
