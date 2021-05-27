package org.wa9nnn.fdcluster.javafx.cluster

import javafx.scene.control.DialogPane
import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import org.wa9nnn.fdcluster.store.network.FdHour
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.scene.control.{ButtonType, Dialog, TableColumn, TableView}
import scalafx.stage.Modality

import javax.inject.Inject

class FdHoursDialog @Inject()(fdHours: FdHours) extends Dialog {
  initModality(Modality.None)
  resizable = true

  private val table = new TableView[Row](fdHours.buffer) {
    columns +=
      new TableColumn[Row, String] {
        text = "FdHour"
        cellValueFactory = r => StringProperty(r.value.fdHour.display)
        prefWidth = 50
      }

    columns ++=
      fdHours.knownNodes.map { nodeAddress =>
        new TableColumn[Row, String]() {
          text = nodeAddress.display
          cellValueFactory = r => StringProperty(r.value.digest(nodeAddress).value.toCell.value)

          prefWidth = 125
        }
      }
  }
  private val dp: DialogPane = dialogPane.value
  dp.getButtonTypes.add(ButtonType.Close)
  dp.setContent(table)


}
