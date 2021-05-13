
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
   * @param product a case class that has Format implicit. Usually in [[org.wa9nnn.fdcluster.model.MessageFormats]]
   * @param writes [[play.api.libs.json.Format]]
   * @tparam T must be a case class
   * @return
   */
  def saveToFile[T: ClassTag](product: T)(implicit writes: Writes[T]): Try[String]

  def loadFromFile[T: ClassTag](f: () => T)(implicit writes: Reads[T]): T
}

/**
 * A simple persistence engine that between case classes and files
 * Files are persisted in the basePath directory using a file name that is the class name (without path)
 *
 * @param fileManager where to write files
 */
class PersistenceImpl @Inject()(fileManager: FileContext) extends Persistence with StructuredLogging {
  val path: Path = fileManager.varDirectory
  Files.createDirectories(path)
  if (!Files.isDirectory(path)) {
    throw new IllegalStateException(s"${path.toAbsolutePath.toString} does not exist or can't be created!")
  }

  /**
   *
   * @param product a case class to be saved
   * @param writes  from [[org.wa9nnn.fdcluster.model.MessageFormats]]
   * @tparam T of case class product.
   * @return file path or error
   */
  override def saveToFile[T: ClassTag](product: T)(implicit writes: Writes[T]): Try[String] = {
    assert(product.isInstanceOf[Product], s"$product is not a case class!")
    Try {
      val path = pathForClass[T]
      Files.createDirectories(path.getParent)
      val sJson = Json.prettyPrint(Json.toJson(product))
      Files.writeString(path, sJson, TRUNCATE_EXISTING, WRITE, CREATE)
      path.toAbsolutePath.toString
    }
  }

  /**
   * load from file
   *
   * @tparam T of case class saved via [[saveToFile()]]
   */
  private def loadFromFile[T: ClassTag]()(implicit writes: Reads[T]): Try[T] = {
    val path1 = pathForClass[T]
    logger.debug(s"Trying $path1")
    val r = Try {
      Json.parse(Files.readString(path1)).as[T]
    }
    r match {
      case Failure(exception) =>
        logger.debug(s"$path1", exception)
      case Success(value) =>
        logger.debug(s"Loaded: $value")
    }
    r
  }

  /**
   *
   * @param tag provided by scala because of [T: ClassTag]
   * @tparam T of a case class that has a Writes[T]. in org.wa9nnn.fdcluster.model.MessageFormats
   * @return path of file for this case class.
   */
  private def pathForClass[T: ClassTag]()(implicit tag: ClassTag[T]): Path = {
    val last = tag.toString.split("""\.""").last
    path.resolve(last + ".json")
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
