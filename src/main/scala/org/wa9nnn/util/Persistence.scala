
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

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.FileContext
import play.api.libs.json._

import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path}
import javax.inject.Inject
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait Persistence {
  /**
   *
   * @param candidate a case class that has Format implicit. Usually in [[org.wa9nnn.fdcluster.model.MessageFormats]]
   * @param p         do this if file product was newer and save actually happened.
   * @param writes    [[play.api.libs.json.Format]]
   * @tparam T must be a case class
   * @return
   */
  def saveToFile[T <: Product : ClassTag](candidate: T)(implicit writes: Writes[T]): Unit

  def loadFromFile[T: ClassTag](f: () => T)(implicit reads: Reads[T]): T
}

/**
 * A simple persistence engine that between case classes and files
 * Files are persisted in the basePath directory using a file name that is the class name (without path)
 *
 * @param fileContext where to write files
 */
class PersistenceImpl @Inject()(fileContext: FileContext) extends Persistence with LazyLogging {
  val path: Path = fileContext.varDirectory
  Files.createDirectories(path)
  if (!Files.isDirectory(path)) {
    throw new IllegalStateException(s"${path.toAbsolutePath.toString} does not exist or can't be created!")
  }

  /**
   *
   * @param candidate a case class to be saved
   * @param writes    from [[org.wa9nnn.fdcluster.model.MessageFormats]]
   * @tparam T of case class product.
   * @return file path or error
   */
  override def saveToFile[T <: Product : ClassTag](candidate: T)(implicit writes: Writes[T]): Unit = {
    Try {
      val path = fileContext.pathForClass[T]
      Files.createDirectories(path.getParent)
      val sJson = Json.prettyPrint(Json.toJson(candidate))
      Files.writeString(path, sJson, TRUNCATE_EXISTING, WRITE, CREATE)
    }
  }

  /**
   * load from file
   *
   * @tparam T of case class saved via [[saveToFile()]]
   */
  private def loadFromFile[T: ClassTag]()(implicit writes: Reads[T]): Try[T] = {
    val path = fileContext.pathForClass[T]
    logger.debug(s"Trying $path")
    val r = Try {
      Json.parse(Files.readString(path)).as[T]
    }
    r match {
      case Failure(exception) =>
        logger.debug(s"$path", exception)
      case Success(value) =>
        logger.debug(s"Loaded: $value")
    }
    r
  }

  /**
   * A bit nicer to use as it always return a value. i.e. client doesn't to process the Try
   *
   * @param f      that will create the default value if not read from the file.
   * @param writes JSON format stuff.
   * @tparam T a case class.
   * @return always a T
   */
  override def loadFromFile[T: ClassTag](f: () => T)(implicit writes: Reads[T]): T = {
    loadFromFile[T].getOrElse(f())
  }
}
