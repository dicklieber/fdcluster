
package org.wa9nnn.fdcluster.javafx.entry

import scalafx.application.Platform
import scalafx.scene.control.{Label, ProgressBar}
import scalafx.scene.layout.{BorderPane, VBox}

import java.time.Instant

/**
 * Show running process status
 * e.g. progress bar status messages
 */
object RunningTaskPane extends RunningTaskInfoConsumer {
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


