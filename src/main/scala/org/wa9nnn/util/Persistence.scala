
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

import com.github.racc.tscg.TypesafeConfig
import org.wa9nnn.fdcluster.{FileManager, FileLocus, FileManagerConfig}
import play.api.libs.json.{Json, Reads, Writes}

import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path, Paths}
import javax.inject.Inject
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait Persistence {
  def saveToFile[T: ClassTag](product: T, pretty: Boolean = true)(implicit writes: Writes[T]): Try[String]
  def loadFromFile[T: ClassTag]()(implicit writes: Reads[T]): Try[T]
}
/**
 * A simple persistence engine that between case classes and files
 * Files are persisted in the [[basePath]] directory using a file name that is the class name (without path)
 * so e.g. [[org.wa9nnn.fdcluster.model.CurrentStation#BandMode(java.lang.String, java.lang.String)]] is saved as <basePath>/BandMode
 *
 * @param fileManager where to write files
 */
class PersistenceImpl @Inject()(fileManager: FileManager) extends Persistence with StructuredLogging {
  val path: Path = fileManager.getPath(FileLocus.`var`)
  Files.createDirectories(path)
  if (!Files.isDirectory(path)) {
    throw new IllegalStateException(s"${path.toAbsolutePath.toString} does not exist or can't be created!")
  }

  /**
   *
   * @param product a case class to be saved
   * @param pretty  true: pretty (formatted multi-line) false: one-line
   * @param writes  from [[org.wa9nnn.fdcluster.model.MessageFormats]]
   * @tparam T of case class product.
   * @return file path or error
   */
  override def saveToFile[T: ClassTag](product: T, pretty: Boolean = true)(implicit writes: Writes[T]): Try[String] = {
    Try {
      if (!product.isInstanceOf[Product]) throw new IllegalArgumentException(s"$product is not a case class!")
      val sJson = if (pretty)
        Json.prettyPrint(Json.toJson(product))
      else
        Json.toJson(product).toString()

      val path = pathForClass[T]
      Files.writeString(path, sJson, TRUNCATE_EXISTING, WRITE, CREATE)
      path.toAbsolutePath.toString
    }
  }

  /**
   * load from file
   *
   * @tparam T of case class saved via [[saveToFile()]]
   */
  def loadFromFile[T: ClassTag]()(implicit writes: Reads[T]): Try[T] = {
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
}
