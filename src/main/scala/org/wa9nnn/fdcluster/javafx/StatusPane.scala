
package org.wa9nnn.fdcluster.javafx

import org.scalafx.extras.onFX
import org.wa9nnn.util.{DelayedFuture, Disposition, StructuredLogging, WithDisposition}
import scalafx.animation.FadeTransition
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, VBox}
import scalafx.util

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.{Duration, FiniteDuration, SECONDS}

/**
 * One of timely status info
 */
@Singleton
class StatusPane @Inject() extends StructuredLogging {
  loggerName("Status")
  private val messageLabel = new Label with WithDisposition {
    styleClass += "statusLine"
  }

  def pane: Pane = new VBox(messageLabel)

  def message(text: String): Unit = message(StatusMessage(text))

  def messageSad(text: String): Unit = message(StatusMessage(text, disposition = Disposition.sad))

  def clear(): Unit = {
    messageLabel.text = ""
  }

  def message(statusMessage: StatusMessage): Unit = {
    onFX {
      messageLabel.styleClass = ObservableBuffer[String](statusMessage.styleClasses)
      messageLabel.disposition(statusMessage.disposition)
      messageLabel.text = statusMessage.text
      statusMessage.nexus.foreach { nexus =>
        logJson(nexus) ++ ("msg" -> statusMessage.text)
      }
    }


    DelayedFuture(statusMessage.duration) {
      onFX {
        fadeTransition.play()
        println("fading...")
      }
    }
  }
  val fadeTransition: FadeTransition = new FadeTransition {
    duration = util.Duration(500)
    node = messageLabel
    fromValue = 1
    toValue = 0
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
                         styleClasses: Seq[String] = Seq.empty,
                         disposition: Disposition = Disposition.neutral,
                         duration: FiniteDuration = Duration(10, SECONDS))