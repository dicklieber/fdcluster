
package org.wa9nnn.fdcluster.javafx

import javafx.util.Duration
import org.wa9nnn.util.{WithDisposition, Disposition, StructuredLogging}
import scalafx.application.Platform
import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, VBox}

import javax.inject.{Inject, Singleton}

/**
 * One of timely status info
 */
@Singleton
class StatusPane @Inject() extends StructuredLogging {
  loggerName("Status")
  private val messageLabel = new Label with WithDisposition {
    styleClass += "statusLine"
  }

  def apply: Pane = new VBox(messageLabel)

  def message(text: String): Unit = message(StatusMessage(text))

  def messageSad(text: String): Unit = message(StatusMessage(text, disposition = Disposition.sad))

  def message(statusMessage: StatusMessage): Unit = {
    Platform.runLater(() => {
      messageLabel.disposition(statusMessage.disposition)
      statusMessage.nexus.foreach { nexus =>
        logJson(nexus) ++ ("msg" -> statusMessage.text)
      }
    })
  }
}

/**
 *
 * @param text             to show in status line
 * @param nexus            were this is from log message if present.
 * @param disposition      true if message is sdd (vs happy) effects styling.
 * @param duration         how long to display.
 */
case class StatusMessage(text: String = "",
                         nexus: Option[String] = None,
                         disposition: Disposition = Disposition.neutral,
                         duration: Duration = Duration.seconds(10))