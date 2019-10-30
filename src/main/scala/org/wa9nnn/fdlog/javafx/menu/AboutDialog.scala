
package org.wa9nnn.fdlog.javafx.menu

import java.time.Instant

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.BuildInfo
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.GridPane


object AboutDialog  extends Dialog with LazyLogging {

  def apply(): Unit = {


     val gridPane: GridPane = new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      add(new Label("Application:"), 0, 0)
      add(new Label("fdlog"), 1, 0)

      add(new Label("Version"), 0, 1)
      add(new Label(BuildInfo.version), 1, 1)

      add(new Label("Git Branch"), 0, 2)
      add(new Label(BuildInfo.gitCurrentBranch), 1, 2)

      add(new Label("Git commit"), 0, 3)
      add(new Label(BuildInfo.gitHeadCommit.getOrElse("--")), 1, 3)

      add(new Label("Built"), 0, 4)
      val buildStamp = Instant.ofEpochMilli(BuildInfo.buildTime.toLong).toString

      add(new Label(buildStamp), 1, 4)

    }
    dialogPane().setContent(gridPane)
    dialogPane().getButtonTypes.add(ButtonType.Close)

    showAndWait()
  }

}
