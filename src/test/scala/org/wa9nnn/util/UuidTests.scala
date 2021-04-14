package org.wa9nnn.util

import play.api.libs.json.{JsValue, Json}

import java.util.UUID

object UuidTests extends App {

  private val uuid: UUID = UUID.randomUUID()
  private val leastSignificantBits: Long = uuid.getLeastSignificantBits
  private val mostSignificantBits: Long = uuid.getMostSignificantBits
  private val bigInt = BigInt(leastSignificantBits)

  private val jsValue: JsValue = Json.toJson(uuid)
  private val str: String = jsValue.toString()
  println(str)

  private val bytes: Array[Byte] = UuidUtil.apply(uuid)
  bytes.size

}
