package org.wa9nnn.util

import org.apache.commons.codec.binary.{Base64InputStream, Base64OutputStream}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import scala.collection.mutable.ArrayBuffer
import scala.util.{Try, Using}

object UuidBinarySerializer {
  /**
   * Produce a Seq[Byte], 16 bytes per UUID.
   *
   * @param source    collection of Ts that contain UUIDs.
   * @param extractor how to get the UUID from the T.
   * @tparam T that the extractor know about.
   * @return 16 bytes per thing.
   */
  def apply[T](source: IterableOnce[T])(extractor: (T) => UUID): Seq[Byte] = {
    source.iterator.foldLeft(new ArrayBuffer[Byte]) { (buf, t) =>
      val bytes = UuidUtil(extractor(t))
      buf.addAll(bytes)
    }.toSeq

  }

}

/**
 * Between Array[Byte] and a base64 encoded compressed string.
 */
object Compressor {
  /**
   *
   * compress
   */
  def apply(bytes: Array[Byte]): String = {
    val baos = new ByteArrayOutputStream()
    Using {
      new GZIPOutputStream(new Base64OutputStream(baos, true, 76, "\n".getBytes()))
    } { gzos =>
      gzos.write(bytes)
      gzos.finish()
      gzos.flush()
    }
    baos.toString
  }

  /**
   * decompress
   */
  def apply(string: String): Try[Array[Byte]] = {
    Using {
      new GZIPInputStream(new Base64InputStream(new ByteArrayInputStream(string.getBytes)))
    } { gzis =>
      gzis.readAllBytes()
    }
  }
}