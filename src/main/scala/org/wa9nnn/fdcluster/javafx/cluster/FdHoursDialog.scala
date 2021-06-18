package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.DialogPane
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.NodeAddress
import scalafx.scene.Node
import scalafx.scene.control.{ButtonType, Dialog, Label, ScrollPane}
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.stage.Modality

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class FdHoursDialog @Inject()(fdHours: FdHours, nodeAddress: NodeAddress) extends Dialog with LazyLogging{
  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm
  title = nodeAddress.display
  initModality(Modality.None)
  resizable = true

  val gridPane: GridPane = new GridPane() {
    styleClass += "clusterTable"
  }
  layout()

  fdHours.layoutVersion.onChange { (_, _, _) =>
    logger.debug("Layout changed")
    onFX {
      layout()
    }
  }

  def layout(): Unit = {
    logger.debug("layout")

    gridPane.children = Seq.empty

    val row = new AtomicInteger()
    gridPane.add(new Label() {
      styleClass += "clusterCornerCell"
    }, 0, 0)

    val headerRow = row.getAndIncrement()
    val ageRow = row.getAndIncrement()
    val countRow = row.getAndIncrement()
    val colInfos: Seq[ColInfo] = fdHours.metadataColumns
    colInfos.foreach { colInfo =>
      val metadata = colInfo.nodeMetadata
      val iCol = colInfo.iCol + 1
      gridPane.add(colInfo.nodeAddress.propertyCell, iCol, headerRow)
      gridPane.add(metadata.ageCell, iCol, ageRow)
      gridPane.add(metadata.qslCountCell, iCol, countRow)
    }
    gridPane.add(new HBox(new Label("Age")) {
      styleClass += "clusterRowHeader"
    }, 0, 1)
    gridPane.add(new HBox(new Label("QSO Count")) {
      styleClass += "clusterRowHeader"
    }, 0, 2)
    // row headers
    val hourRowStart = row.getAndIncrement()
    fdHours.rows.map(fdHour => fdHour.propertyCell).zipWithIndex.foreach { case (node: PropertyCell, iRow) =>
      gridPane.add(node, 0, iRow + hourRowStart)
    }

    // body
    for {
      fdHour <- fdHours.rows
      iRow = row.getAndIncrement()
      colInfo <- colInfos
    } {
      val node: Node = fdHours.get(fdHour, colInfo.nodeAddress).getOrElse(new Label("-"))
      gridPane.add(node, colInfo.iCol + 1, iRow-1)
    }
  }

  private val dp: DialogPane = dialogPane.value
  dp.getButtonTypes.add(ButtonType.Close)
  dp.setContent(new ScrollPane() {
    content = gridPane
  })
  dp.getStylesheets.add(cssUrl)
}
