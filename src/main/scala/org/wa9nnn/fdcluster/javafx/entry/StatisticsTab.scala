
package org.wa9nnn.fdcluster.javafx.entry

import akka.actor.{ActorSystem, Cancellable}
import com.google.inject.Inject
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.FieldCount.Sorter
import org.wa9nnn.fdcluster.{FieldCount, QsoCountCollector, StatCollector}
import org.wa9nnn.util.StructuredLogging
import scalafx.geometry.Orientation
import scalafx.scene.control.{Button, Label, Tab}
import scalafx.scene.layout._

import java.time.Duration
import javax.inject.Singleton

@Singleton
class StatisticsTab @Inject()(qsoCountCollector: QsoCountCollector, val actorSystem: ActorSystem) extends AutoRefreshTab {
  //todo elapsed/remaining time in contest.
//  implicit val executor = actorSystem.dispatcher

//  private val task = new Runnable {
//    def run() {
//      logger.trace("Refresh:scheduled")
//      onFX {
//        refresh()
//      }
//    }
//  }
//  var timer: Option[Cancellable] = None
//
//  selected.onChange { (_, _, isSelectd) =>
//    if (isSelectd) {
//      logger.trace("Refresh:selected")
//      refresh()
//      timer = Some(actorSystem.scheduler.scheduleAtFixedRate(
//        initialDelay = Duration.ofSeconds(5),
//        interval = Duration.ofSeconds(5),
//        runnable = task,
//        executor = executor)
//      )
//    } else {
//      logger.trace("Cancel Timer")
//      timer.foreach(_.cancel())
//    }
//  }
//

  private var sorter: Sorter = FieldCount.byCount.sorter

  private val byCountButton = new Button(FieldCount.byCount.name) {
    onAction = _ => {
      sorter = FieldCount.byCount.sorter
      refresh()
    }
  }
  private val byFieldButton = new Button(FieldCount.byField.name) {
    onAction = _ => {
      sorter = FieldCount.byField.sorter
      refresh()
    }
  }

  val allStats: FlowPane = new FlowPane() {
    new TilePane() {
      styleClass += "statPane"
      orientation = Orientation.Vertical
      prefHeight = 200

    }
  }

  text = "Statistics"
  content = new BorderPane() {
    top = new HBox(byCountButton, byFieldButton)
    center = allStats
  }
  closable = false

   def refresh(): Unit = {
    allStats.children = qsoCountCollector.collectors.map(new CollectorPane(_, sorter))
  }
}

class CollectorPane(collector: StatCollector, sort: Sorter) extends BorderPane {
  styleClass += "statPane"
  top = new HBox {
    styleClass += "statPaneTitleBox"
    children += new Label(collector.name) {
      tooltip = collector.tooltip
      styleClass += "statPaneTitle"
    }
  }

  center = new TilePane() {
    styleClass += "oneStatTilePane"
    orientation = Orientation.Vertical
    prefHeight = 200

    children = collector.data(sort).map { fieldCount =>
      val field = fieldCount.field match {
        case "" => "??"
        case x => x
      }
      new HBox(
        new Label(field + ":") {
          styleClass += "statLabel"

        },
        new Label(f"${fieldCount.count}%,5d") {
          styleClass += "statValue"
        }
      ) {
        styleClass += "statCell"
      }
    }
  }
}

