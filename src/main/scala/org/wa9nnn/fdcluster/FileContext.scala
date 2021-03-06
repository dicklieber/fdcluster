
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

import org.wa9nnn.fdcluster.model.{ContestProperty, ExportFile, NodeAddress}
import org.wa9nnn.util.{Persistence, PersistenceImpl}
import play.api.libs.json.{Reads, Writes}

import java.nio.file.{Files, Path, Paths}
import java.security.SecureRandom
import java.time.ZonedDateTime
import scala.reflect.ClassTag

/**
 * All access to various files should go through this.
 */
class FileContext extends Persistence {
  val userDir: Path = Paths.get(System.getProperty("user.home")).toAbsolutePath
  val instance: Option[Int] = Option(System.getProperty("instance")).map(_.toInt)
  val directory: Path = userDir.resolve(s"fdcluster${instance.getOrElse("")}")

  val logsDirectory: Path = directory.resolve("logs")
  Files.createDirectories(logsDirectory)
  val logFile: Path = logsDirectory.resolve("fdcluster.log")

  System.setProperty("log.file.path", logFile.toString)
  private val secureRandom = new SecureRandom()
  System.setProperty("instance", secureRandom.nextInt().toString)


  val persistenceDelegate: Persistence = new PersistenceImpl(this)

  override def saveToFile[T <: Product : ClassTag](product: T)(implicit writes: Writes[T]): Unit = persistenceDelegate.saveToFile(product)

  override def loadFromFile[T: ClassTag](f: () => T)(implicit reads: Reads[T]): T = persistenceDelegate.loadFromFile(f)

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
    ExportFile(dir, s"$fileBase.${extension.dropWhile(_ == '.')}")
  }


  /**
   *
   * @param tag provided by scala because of [T: ClassTag]
   * @tparam T of a case class that has a Writes[T]. in org.wa9nnn.fdcluster.model.MessageFormats
   * @return path of file for this case class.
   */
   def pathForClass[T: ClassTag]()(implicit tag: ClassTag[T]): Path = {
    val last = tag.toString.split("""\.""").last
    varDirectory.resolve(last + ".json")
  }

}



