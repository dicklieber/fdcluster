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

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.javafx.FdCluster.{getClass, ourScene}
import org.wa9nnn.fdcluster.{BuildInfo, FileManager}
import org.wa9nnn.fdcluster.javafx.GridOfControls
import scalafx.scene.control.{Hyperlink, _}
import scalafx.scene.layout.{HBox, VBox}

import java.awt.Desktop
import java.lang.management.ManagementFactory
import java.net.URI
import java.time.Instant

object AboutDialog extends Dialog with LazyLogging {
  title = s"About ${BuildInfo.name}"

  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm


  def apply(fileManager: FileManager): Unit = {
    val desktop = Desktop.getDesktop

    val goc = new GridOfControls()

    goc.add("Application", BuildInfo.name)
    goc.add("Version", BuildInfo.version)
    goc.add("Git Branch", BuildInfo.gitCurrentBranch)
    goc.add("Git commit", BuildInfo.gitHeadCommit.getOrElse("--"))
    goc.add("Built", Instant.ofEpochMilli(BuildInfo.buildTime.toLong).toString)
    goc.add("Java Home", new Hyperlink(System.getenv("JAVA_HOME")) {
      onAction = event => {
        desktop.open(fileManager.directory.toFile)
      }
    })
    goc.add("Java Version", ManagementFactory.getRuntimeMXBean.getVmVersion)
    goc.add("App Directory", new Hyperlink(fileManager.directory.toString) {
      onAction = event => {
        desktop.open(fileManager.directory.toFile)
      }
    })
    goc.add("Source Code", new Hyperlink("https://github.com/dicklieber/fdcluster") {
      onAction = event => {
        desktop.browse(new URI("https://github.com/dicklieber/fdcluster"))
      }
    })
    goc.add("Blame this guy", new Hyperlink("Dick Lieber WA9NNN") {
      onAction = event => {
        if (desktop.isSupported(Desktop.Action.MAIL)) {
          val uri = s"mailto:${BuildInfo.maintainer}?subject=${BuildInfo.name}%20version:${BuildInfo.version}"
          val mailto = new URI(uri)
          desktop.mail(mailto)
        }
      }
    })
    val dialogPane1 = dialogPane()
    dialogPane1.getStylesheets.add(cssUrl)

    dialogPane1.setContent(new VBox(goc,
      new HBox(
        new Label("© 2021  Dick Lieber, WA9NNN") {
          styleClass += "parenthetic"

        },
        new Hyperlink("Licensed under gpl-3.0") {
          styleClass += "parenthetic"
          onAction = event => {
            desktop.browse(new URI("http://www.gnu.org/licenses/gpl-3.0.html"))
          }
        }
      ){
        styleClass += "alignedLine"
      }
    )
    )
    dialogPane1.getButtonTypes.add(ButtonType.Close)

    showAndWait()
  }

}
