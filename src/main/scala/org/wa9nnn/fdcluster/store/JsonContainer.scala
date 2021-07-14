package org.wa9nnn.fdcluster.store

import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.output.ByteArrayOutputStream
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.store.JsonContainer.node
import org.wa9nnn.fdcluster.store.network.MessageDecoder
import play.api.libs.json.{Json, Writes}

import java.io.ByteArrayInputStream
import java.security.SecureRandom
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import scala.reflect.{ClassTag, classTag}
import scala.util.{Try, Using}
import org.wa9nnn.fdcluster.store.JsonContainer.node
import org.wa9nnn.fdcluster.store.JsonContainer.sn
/**
 * Something that can be sent accross the network.
 * Always use the apply methods to creare instances.
 *
 * @param className
 * @param json
 */
case class JsonContainer private(className: String, json: String, stamp: Instant = Instant.now()) {
  def bytes: Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val gzInputStream: GZIPOutputStream = new GZIPOutputStream(baos)
    gzInputStream.write(Json.toJson(this)
      .toString()
      .getBytes)
    gzInputStream.close()
    baos.close()
    val prefix: Array[Byte] = f"$node%x:${sn.incrementAndGet()}#".getBytes
    prefix ++: baos.toByteArray
  }

  def toByteString: ByteString = {
    ByteString(bytes)
  }

  def received(): Option[Any] = {
    MessageDecoder(this)
  }
}

object JsonContainer extends LazyLogging {
  val node: Long = new SecureRandom().nextLong()
  val sn = new AtomicLong()

  def apply(byteString: ByteString): Try[JsonContainer] = {
    apply(byteString.toArray)
  }

  def apply(bytes: Array[Byte]): Try[JsonContainer] = {
    val prefix = bytes.takeWhile(_ != '#')
    val info = new String(prefix)
    val remaining = bytes.slice(prefix.length + 1, Integer.MAX_VALUE)

    val triedMessage = Using(new GZIPInputStream(new ByteArrayInputStream(remaining))) { gzis =>
      val jsValue = Json.parse(gzis)
      jsValue.as[JsonContainer]
    }
    triedMessage.failed.foreach(et =>
      logger.warn(s"Processing gzip: ${et.getMessage}")
    )
    triedMessage
  }

  def apply[T: ClassTag](canBeJson: T)(implicit tjs: Writes[T]): JsonContainer = {
    val clazz: Class[_] = classTag[T].runtimeClass
    val className: String = clazz.getName
    val json = Json.toJson(canBeJson).toString()
    new JsonContainer(className, json)
  }
}

