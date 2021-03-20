
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

package org.wa9nnn.fdcluster.model

import java.nio.file.{Files, Path, Paths}

//Message to send to [[StoreActor]] to cause ADIF export to happen.
case class AdifExportRequest(exportFile: ExportFile = ExportFile())

/**
 * [[directory]] and [[fileName]] are kept separate so UI can easily allow user to edit both.
 * @param directory to be resolved with filename.
 * @param fileName to save under.
 */
case class ExportFile(directory: String = System.getProperty("user.home"), fileName: String = "") {
  def validate: Option[String] = {
    if (fileName.isBlank)
      Some("Filename cannot be empty!")
    else
      None
  }

  def path: Path = {
    Files.createDirectories(directoryPath)
    if (!Files.isWritable(directoryPath)) throw new IllegalStateException(s"Can't create or is not-writable: $directoryPath")
    directoryPath.resolve(fileName)
  }

  def directoryPath: Path = Paths.get(directory).toAbsolutePath

  def absoluteDirectory: String = directoryPath.toString
}


