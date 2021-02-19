
package org.wa9nnn.util

import com.github.racc.tscg.TypesafeConfig
import play.api.libs.json.{Json, Reads, Writes}

import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path, Paths}
import javax.inject.Inject
import scala.reflect.ClassTag
import scala.util.Try


/**
 * A simple persistence engine that between case classes and files
 * Files are persisted in the [[basePath]] directory using a file name that is the class name (without path)
 * so e.g. [[org.wa9nnn.fdcluster.model.BandMode#BandMode(java.lang.String, java.lang.String)]] is saved as <basePath>/BandMode
 *
 * @param basePath where to write files
 */
class Persistence @Inject()( @TypesafeConfig("fdcluster.configPath") configPath: String) extends JsonLogging {
  val path: Path = Paths.get(configPath)
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
  def saveToFile[T: ClassTag](product: T, pretty: Boolean = true)(implicit writes: Writes[T]): Try[String] = {
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
    Try(Json.parse(Files.readString(pathForClass[T])).as[T])
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
