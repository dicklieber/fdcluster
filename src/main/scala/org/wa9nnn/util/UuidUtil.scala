package org.wa9nnn.util

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
  implicit def u2bytes(uuid: UUID): Array[Byte] = apply(uuid)
}