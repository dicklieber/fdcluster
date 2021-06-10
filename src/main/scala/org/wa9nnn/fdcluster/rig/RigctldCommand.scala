package org.wa9nnn.fdcluster.rig

import org.apache.commons.text.StringSubstitutor
import scalafx.beans.binding.{Bindings, ObjectBinding}
import scalafx.scene.control.{ComboBox, Label, TextField}

import scala.jdk.CollectionConverters._

object RigctldCommand {
  def apply(params: RigctldLaunchPrameters): String = {
    val valuesMap: Map[String, Any] = Seq(
      "modelId" -> params.rigModel.number,
      "speed" -> params.baudRate,
      "deviceName" -> params.port.map(_.port).getOrElse("missing")
    ).toMap
    val ss: StringSubstitutor = new StringSubstitutor(valuesMap.asJava, "<", ">")
    // ${fdcluster.rig.rigctldApp} -m <modelId> -r <deviceName>
    val commandLine = ss.replace(params.rigctldCommand)
    commandLine
  }
}

class RigctldCommand(rigModelCb: ComboBox[RigModel], speed: ComboBox[String], serialPortCB: ComboBox[SerialPort], rigctldCmd: TextField, sample: Label) {
  sample.text.value = "rigctld command line"

  val b: ObjectBinding[String] = Bindings.createObjectBinding[String](
    () => {
      val cmdLine: String = RigctldCommand(new RigctldLaunchPrameters {
        val rigModel = rigModelCb.value.value

        val port = Some(serialPortCB.value.value)

        val baudRate = speed.value.value


        val rigctldCommand = rigctldCmd.text.value
      })
      cmdLine

    }, rigModelCb.value, speed.value, serialPortCB.value, rigctldCmd.text
  )

  sample.text <== b

}


