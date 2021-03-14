
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

package org.wa9nnn.fdcluster.javafx.menu

import java.time.Instant

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.BuildInfo
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
      add(new Label("fdcluster"), 1, 0)

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
