
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

package org.wa9nnn.util

import _root_.scalafx.application.JFXApp.Parameters

class CommandLineScalaFxImpl(parameters: Parameters) extends CommandLine {
  val noArg: Set[String] = parameters.unnamed.map(_.dropWhile(_ == '-')).toSet

  /**
   *
   * @param name of command line parameter without leading dashes.
   * @return true if
   */
  def is(name: String): Boolean = {
    noArg.contains(name)
  }

  def getString(name: String): Option[String] = {
    parameters.named.get(name)
  }

  override def getInt(name: String): Option[Int] = {
    getString(name).map(_.toInt)
  }
}

trait CommandLine {
  def is(name: String): Boolean

  def getString(name: String): Option[String]

  def getInt(name: String): Option[Int]
}