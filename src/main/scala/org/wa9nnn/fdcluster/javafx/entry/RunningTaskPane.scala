
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

package org.wa9nnn.fdcluster.javafx.entry

import scalafx.application.Platform
import scalafx.scene.control.{Label, ProgressBar}
import scalafx.scene.layout.{BorderPane, VBox}

import java.time.Instant

/**
 * Show running process status
 * e.g. progress bar status messages
 */
class RunningTaskPane extends RunningTaskInfoConsumer {
  private val taskName = new Label() {
    styleClass += "title"
  }
  private val topMessage = new Label()
  private val bottomMessage = new Label()
  private val progressBar = new ProgressBar() {
    styleClass += "progress"
  }
  val messages: VBox = new VBox() {
    children.addAll(topMessage, bottomMessage)
  }

  val pane: BorderPane = new BorderPane() {
    styleClass += "infoPane"
    top = taskName
    center = messages
    bottom = progressBar

  }
  done()

  def update(info: RunningTaskInfo): Unit = {
    Platform.runLater {
      taskName.text = info.taskName
      progressBar.progress = info.progress
      info.top.applyToLabel(topMessage)
      info.bottom.applyToLabel(bottomMessage)
      pane.setVisible(true)
    }
  }

  override def done(): Unit = {
    Platform.runLater {
      taskName.text = ""
      topMessage.text = ""
      bottomMessage.text = ""
      progressBar.progress = -1
      pane.setVisible(false)
    }
  }
}

class NullRunningTaskConsumer extends RunningTaskInfoConsumer {
  override def update(info: RunningTaskInfo): Unit = {
  }


  override def done(): Unit = {}
}

trait RunningTaskInfoConsumer {
  def update(info: RunningTaskInfo): Unit

  def done(): Unit
}

case class RunningTaskInfo(taskName: String, progress: Double = -1.0, top: StyledText = StyledText(), bottom: StyledText = StyledText(), started: Instant = Instant.now()) {
  def apply(progress: Double, top: StyledText): RunningTaskInfo = {
    copy(progress = progress, top = top)
  }
}

case class StyledText(text: String, cssStyle: String*) {
  def applyToLabel(control: Label): Unit = {
    control.text = text
    control.styleClass = cssStyle.toIterable
  }
}

object StyledText {
  def apply(): StyledText = new StyledText("")

  def sad(text: String): StyledText = {
    new StyledText(text, "sad")
  }
}


