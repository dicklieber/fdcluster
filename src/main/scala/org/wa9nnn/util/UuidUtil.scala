package org.wa9nnn.util

import org.apache.commons.codec.binary.Base64
import org.wa9nnn.fdcluster.model.Exchange
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

import java.nio.ByteBuffer
import java.util.UUID
import scala.language.implicitConversions

object UuidUtil {
  def apply(bytes: Array[Byte]): UUID = {
    val bb = ByteBuffer.wrap(bytes)
    val firstLong = bb.getLong
    val secondLong = bb.getLong
    new UUID(firstLong, secondLong)
  }

  def apply(uuid: UUID): Array[Byte] = {
    val bb = ByteBuffer.wrap(new Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    bb.array
  }

  implicit def uuidToBytes(uuid: UUID): Array[Byte] = apply(uuid)

  def toBase64(uuid: UUID): String = {
    Base64.encodeBase64URLSafeString(apply(uuid))
  }

  def fromBase64(b64: String): UUID = {
    apply(Base64.decodeBase64(b64))
  }


  /**
   * to make JSON a bit more compact
   */
  implicit val uuidFormat: Format[UUID] = new Format[UUID] {
    override def reads(json: JsValue): JsResult[UUID] = {
      val ss = json.as[String]
      try {
        JsSuccess(UuidUtil.fromBase64(ss))
      } catch {
        case e:Exception =>
          JsError(s"UUID: $ss could not base64 UUID!")
      }
    }

    override def writes(uuid: UUID): JsValue = {
      JsString(UuidUtil.toBase64(uuid))
    }
  }

}