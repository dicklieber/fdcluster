package org.wa9nnn.fdcluster.store

import org.apache.commons.io.output.ByteArrayOutputStream
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.store.network.MessageDecoder
import play.api.libs.json.{Json, Writes}

import java.io.ByteArrayInputStream
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import scala.reflect.{ClassTag, classTag}
import scala.util.{Try, Using}

/**
 * Something that can be sent accross the network.
 * Always use the apply methods to creare instances.
 *
 * @param className
 * @param json
 */
case class JsonContainer private(className: String, json: String) {
  def bytes: Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val gzInputStream: GZIPOutputStream = new GZIPOutputStream(baos)
    gzInputStream.write(Json.toJson(this)
      .toString()
      .getBytes)
    gzInputStream.close()
    baos.close()
    baos.toByteArray
  }

  def received():Option[Any] = {
    MessageDecoder(this)
  }
}

object JsonContainer {
  def apply(bytes: Array[Byte]): Try[JsonContainer] = {
    Using(new GZIPInputStream(new ByteArrayInputStream(bytes))) { gzis =>
      val jsValue = Json.parse(gzis)
      jsValue.as[JsonContainer]
    }
  }

  def apply[T: ClassTag](canBeJson: T)(implicit tjs: Writes[T]): JsonContainer = {
    val clazz: Class[_] = classTag[T].runtimeClass
    val className: String = clazz.getName
    val json = Json.toJson(canBeJson).toString()
    JsonContainer(className, json)
  }
}
