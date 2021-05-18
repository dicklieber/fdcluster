
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.javafx

import _root_.scalafx.animation.FadeTransition
import _root_.scalafx.collections.ObservableBuffer
import _root_.scalafx.scene.control.Label
import _root_.scalafx.scene.layout.{Pane, VBox}
import _root_.scalafx.util
import com.typesafe.scalalogging.LazyLogging
import org.scalafx.extras.onFX
import org.wa9nnn.util.{DelayedFuture, Disposition, WithDisposition}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.{Duration, FiniteDuration, SECONDS}

/**
 * One of timely status info
 */
@Singleton
class StatusPane @Inject() extends LazyLogging {
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
      messageLabel.styleClass = ObservableBuffer.from(statusMessage.styleClasses)
      messageLabel.disposition(statusMessage.disposition)
      messageLabel.text = statusMessage.text
    }

    DelayedFuture(statusMessage.duration) {
      onFX {
        fadeTransition.play()
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