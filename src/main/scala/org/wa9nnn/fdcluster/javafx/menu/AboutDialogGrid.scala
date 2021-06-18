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

import _root_.scalafx.scene.control.{Hyperlink, _}
import _root_.scalafx.scene.layout.{HBox, VBox}
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.store.network.multicast.MulticastIo
import org.wa9nnn.fdcluster.{AppInfo, BuildInfo, FileContext}
import scalafx.geometry.Insets

import java.awt.Desktop
import java.io.File
import java.lang.management.ManagementFactory
import java.net.{Inet4Address, InterfaceAddress, NetworkInterface, URI}
import java.time.{Duration, Instant}
import javax.inject.{Inject, Singleton}
import scala.collection.mutable
import scala.jdk.CollectionConverters._

@Singleton
class AboutDialogGrid @Inject()(appInfo: AppInfo,
                                fileManager: FileContext,
                                nodeAddress: NodeAddress,
                                multicastThing: MulticastIo) extends Dialog with LazyLogging {
  title = s"About ${BuildInfo.name}"
  resizable = true
  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm

  dialogPane.value.getButtonTypes.add(ButtonType.Close)
  implicit val desktop: Desktop = Desktop.getDesktop


  def apply(): Unit = {

    val goc = new GridOfControls()

    goc.add("Application", BuildInfo.name)
    goc.add("Version", BuildInfo.version)
    goc.add("UpTime", Duration.between(appInfo.started, Instant.now()))
    goc.add("Git Branch", BuildInfo.gitCurrentBranch)
    goc.add("Git commit", BuildInfo.gitHeadCommit.getOrElse("--"))
    goc.addControl("Source Code", new Hyperlink("https://github.com/dicklieber/fdcluster") {
      onAction = event => {
        desktop.browse(new URI("https://github.com/dicklieber/fdcluster"))
      }
    })

    goc.add("Built", BuildInfo.buildInstant)
    goc.addControl("Java Home", new Hyperlink(System.getenv("JAVA_HOME")) {
      onAction = event => {
        val javaHome = new File(this.text.value)
        desktop.open(javaHome)
      }
    })
    goc.add("Java Version", ManagementFactory.getRuntimeMXBean.getVmVersion)
    goc.add("JavaFx Version", System.getProperty("javafx.version"))
    goc.addControl("App Directory", new Hyperlink(fileManager.directory.toString) {
      onAction = event => {
        desktop.open(fileManager.directory.toFile)
      }
    })
    goc.addControl("Our HTTP", new Hyperlink(nodeAddress.url.toExternalForm()) {
      onAction = event => {
        desktop.browse(nodeAddress.url.toURI)
      }
    })
    val args = ManagementFactory.getRuntimeMXBean.getInputArguments.asScala
    val control = new TextArea(args.mkString("\n")) {
      prefRowCount = args.size + 1
      prefColumnCount = 70
      editable = false
    }
    goc.addControl("Command Line", control)

    val ifList = NetworkInterface.getNetworkInterfaces
      .asScala
      .toList
//      .filter(_.isUp)
      .flatMap { ni: NetworkInterface =>
        val addresses: mutable.Seq[InterfaceAddress] = ni.getInterfaceAddresses
          .asScala
          .filter {
            _.getAddress.isInstanceOf[Inet4Address]
          }
        addresses.map { address: InterfaceAddress =>
          val host = address.getAddress.getHostAddress
          f"${ni.getName}%-10s\t$host%-10s"

        }
      }

    val netIfControl = new TextArea(ifList.mkString("\n")) {
      prefRowCount = ifList.size
      prefColumnCount = 70
      editable = false
    }
    goc.addControl("Network Interfaces", netIfControl)

    val mcastInterface: String = multicastThing.networkInterface.getInetAddresses.asScala.toList.head.getHostAddress
    goc.addText("Multicast Interface", mcastInterface)

    goc.addControl("Log", new Hyperlink(fileManager.logFile.toString ){
      onAction = _ => {
        desktop.open(fileManager.logFile.toFile)
      }
    })
    goc.addControl("Blame this guy", new Hyperlink("Dick Lieber WA9NNN") {
      onAction = event => {
        if (desktop.isSupported(Desktop.Action.MAIL)) {
          val uri = s"mailto:${BuildInfo.maintainer}?subject=${BuildInfo.name}%20version:${BuildInfo.version}"
          val mailto = new URI(uri)
          desktop.mail(mailto)
        }
      }
    })

    goc.add("Credits", Credits(
      Credit("Icons made by", "https://www.freepik.com", Some("Freepik"))
    ))

    //    goc.add("ClassPath", System.getProperty("java.class.path").split(":").mkString("\n"))

    val dialogPane1 = dialogPane()
    dialogPane1.getStylesheets.add(cssUrl)

    dialogPane1.setContent(new VBox(goc,
      new HBox(
        new Label("© 2020, 2021  Dick Lieber, WA9NNN") {
          styleClass += "parenthetic"

        },
        new Hyperlink("Licensed under gpl-3.0") {
          styleClass += "parenthetic"
          onAction = event => {
            desktop.browse(new URI("http://www.gnu.org/licenses/gpl-3.0.html"))
          }
        }
      ) {
        styleClass += "alignedLine"
      }
    )
    )
    showAndWait()
  }
}

case class Credits(credits: Credit*) extends GridOfControls(5 -> 5, Insets(2)) {

  credits.foreach { credit =>
    addControl(credit.name, credit.hyperLink)
  }
}

case class Credit(name: String, url: String, link: Option[String] = None)(implicit desktop: Desktop) {
  def hyperLink: Hyperlink = {
    new Hyperlink(link.getOrElse(url)) {
      onAction = event => {
        val uri = new URI(url)
        desktop.browse(uri)
      }
    }
  }
}

