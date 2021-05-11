
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

package org.wa9nnn.fdcluster

import org.wa9nnn.fdcluster.model.{ContestProperty, ExportFile}

import java.nio.file.{Files, Path, Paths}
import java.time.ZonedDateTime

/**
 * All access to various files should go through this.
 */
class FileManager  {
  val userDir: Path = Paths.get(System.getProperty("user.home")).toAbsolutePath
  val instance: Int = System.getProperty("instance", "1").toInt
  val directory: Path = userDir.resolve(s"fdcluster${instance}")

  val logsDirectory: Path = directory.resolve("logs")
  Files.createDirectories(logsDirectory)
  val logFile: String = logsDirectory.resolve("fdcluster.log").toString

  System.setProperty("log.file.path", logFile.toString)

  /**
   *
   * @return where to keep settings
   */
  def varDirectory: Path = directory.resolve("var")

  def journalDir: Path = directory.resolve("journal")

  /**
   *
   * @param extension       with or without leading dot.
   * @param contestProperty to make contest-specific names.
   * @return with default directory and filename.
   */
  def defaultExportFile(extension: String, contestProperty: ContestProperty): ExportFile = {
    val year = ZonedDateTime.now().getYear
    val contestName = contestProperty.contestName
    val fileBase = s"$contestName-$year"
    val dir: String = directory.resolve(contestName).toAbsolutePath.toString
    ExportFile(dir, s"$fileBase.${extension.dropWhile(_ =='.')}")
  }

  def httpPort:Int =  {
      8080 + instance
  }

}



