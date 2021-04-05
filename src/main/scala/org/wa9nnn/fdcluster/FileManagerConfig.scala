
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

import com.github.racc.tscg.TypesafeConfig
import org.wa9nnn.fdcluster.model.{ContestProperty, ExportFile}

import java.nio.file.{Path, Paths}
import javax.inject.{Inject, Singleton}

/**
 * All access to various files should go through this.
 */
@Singleton
class FileManager @Inject()(@TypesafeConfig("directory") dir: String) {

  val directory = Paths.get(dir).toAbsolutePath

  /**
   *
   * @return where to keep settings
   */
  def varDirectory: Path = directory.resolve("var")

  def journalFile: Path = directory.resolve("journal.json")

  /**
   *
   * @param extension       without leading dot.
   * @param contestProperty to make contest-specific names.
   * @return with default directory and filename.
   */
  def defaultExportFile(extension: String)(implicit contestProperty: ContestProperty): ExportFile = {
    val fileBase = contestProperty.fileBase
    val dir: String = directory.resolve(fileBase).toAbsolutePath.toString
    ExportFile(dir, s"$fileBase.$extension")
  }
}


