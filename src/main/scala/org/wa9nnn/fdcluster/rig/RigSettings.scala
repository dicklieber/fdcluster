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

package org.wa9nnn.fdcluster.rig

import org.wa9nnn.fdcluster.model.MessageFormats._
import play.api.libs.json.Json

import java.time.Instant

case class RigSettings(rigModel: RigModel = RigModel(),
                       port: Option[SerialPort] = None,
                       baudRate: String = "9600",
                       enable: Boolean = false,
                       launchRigctld: Boolean = false,
                       rigctldCommand: String = "rigctld -m <modelId>  -s <speed> -r <deviceName>",
                       rigctldHostPort: String = "localhost:4532",
                       stamp: Instant = Instant.now()) extends RigctldLaunchPrameters

/**
 * Need to launch rigtcld
 */
trait RigctldLaunchPrameters {
  def rigModel: RigModel
  def port: Option[SerialPort]
  def baudRate: String
  def rigctldCommand:String
}