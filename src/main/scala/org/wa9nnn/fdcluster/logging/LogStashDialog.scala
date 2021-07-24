/*
 *   Copyright (C) @today.year  Dick Lieber, WA9NNN
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.logging

import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.util.HostPort
import scalafx.Includes._
import scalafx.scene.control._

import javax.inject.Inject


class LogStashDialog @Inject()(logstashProperty: LogstashProperty) extends Dialog[EnabledDestination] {
  val dp: DialogPane = dialogPane()
  private val cdc: EnabledDestination = logstashProperty.value
  private val gridOfControls = new GridOfControls()
  val logstashDestination: TextField = new TextField {
    text.value = cdc.hostPort.toString
    tooltip = "Host and port of LogStash. e.g. 192.168.0.200:5044"
  }
  val logstashEnable: CheckBox = new CheckBox("Enable") {
    selected = cdc.enabled
  }
  gridOfControls.addControl("LogStash", logstashDestination, logstashEnable)

  dp.setContent(gridOfControls)

  val ButtonTypeSave = new ButtonType("Save")
  dp.getButtonTypes.addAll(ButtonTypeSave, ButtonType.Cancel)


  resultConverter = dialogButton => {
    val r: EnabledDestination = if (dialogButton == ButtonTypeSave) {
      EnabledDestination(HostPort(logstashDestination.text.value, 5044), logstashEnable.selected.value)
    }
    else
      null
    r
  }


  showAndWait() match {
    case Some(value) =>
      val destination: EnabledDestination = value.asInstanceOf[EnabledDestination]
     logstashProperty.value = destination
    case None =>

  }


}
