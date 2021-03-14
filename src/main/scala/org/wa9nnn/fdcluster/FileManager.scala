
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

import com.typesafe.config.{Config, ConfigFactory}
import org.wa9nnn.fdcluster.FileManagerConfig.logFilePropertyName
import org.wa9nnn.fdcluster.model.Contest

import java.nio.file.{Files, Path, Paths}

/**
 * All access to various files should go through this.
 */
class FileManagerConfig(config: Config, val contest: Contest) extends FileManager {
  def this() {
    this(ConfigFactory.load(), Contest())
  }

  override def directory: Path = Paths.get(config.getString("directory"))

  val logFilePath: Path = {
    val path = directory.resolve("logs").resolve("fdcluster.log")
    Files.createDirectories(path.getParent)
    val logFile = path.toAbsolutePath.toString
    System.setProperty(logFilePropertyName, logFile)
    path
  }

}

object FileManagerConfig {

  val logFilePropertyName = "log.file.path"
}

trait FileManager {
  def getString(locus: FileLocus):String = {
    getPath(locus ).toString
  }

  def directory: Path

  def contest: Contest

  val map: Map[FileLocus, Path] = FileLocus.values().map { locus =>
    if (locus == FileLocus.contest) {
      locus -> directory.resolve(contest.toString)
    } else
      locus -> directory.resolve(locus.getPathPiece)
  }.toMap
  map.foreach { case (_, path) =>
    if (!Files.isWritable(path))
      Files.createDirectories(path.getParent)
  }

  def getPath(locus: FileLocus): Path = {
    map(locus)
  }
}


