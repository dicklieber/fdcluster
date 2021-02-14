package org.wa9nnn.fdcluster.rig

import org.wa9nnn.fdcluster.model.MessageFormats._
import play.api.libs.json.Json

import java.time.Instant

case class RigSettings(rigModel: RigModel, serialPortSettings: SerialPortSettings, stamp: Instant = Instant.now()) {
  def encodeJson: String = {
    Json.toJson(this).toString()
  }
}

object RigSettings {
  def apply(): RigSettings = RigSettings(RigModel(), SerialPortSettings())

  def decodeJson(json: String): RigSettings = {
    Json.parse(json).as[RigSettings]
  }

}
