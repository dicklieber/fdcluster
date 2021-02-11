
package org.wa9nnn.fdcluster.rig

import com.fazecast.jSerialComm.SerialPort
import org.wa9nnn.util.JsonLogging

object Serial extends App {
  println("all ports:")
  ports.foreach(p => println(s"\t$p"))
  println("Usefull:")
  ports.filter(_.useful)foreach(p => println(s"\t$p"))

  def ports: Seq[SerialPortWrapper] = {
    SerialPort.getCommPorts
      .map(SerialPortWrapper)
      .sorted
  }
}

case class SerialPortWrapper(serialPort: SerialPort) extends NamedSerialPort[SerialPortWrapper] {
  override val name: String = serialPort.getSystemPortName

  override def description: String = serialPort.getPortDescription
}

trait NamedSerialPort[T <: NamedSerialPort[T]] extends Ordered[T] with JsonLogging {
  implicit def name: String

  def description: String

  override def toString: String = s"$name ($description)"

  def compare(that: T): Int = {
    name.compareToIgnoreCase(that.name)
  }

  def dd(cmd: String, ret: Boolean)(implicit name: String): Boolean = {
//    println(s"$name: $cmd => $ret")
    ret
  }

  lazy val useful: Boolean = {
    val linuxBad = """ttyp.+""".r
    val microsoftWidows = """COM\d+""".r
    val macOS = """tty\.usb.*""".r
    val macOSBT = """.*Bluetooth.*""".r
    val raspberryPi = """ttyUSB.*""".r

    name match {
      case linuxBad(_) => dd("linuxBad", ret = false)
      case microsoftWidows(_) => dd("microsoftWidows", ret = true)
      case "tty.Bluetooth-Incoming-Port" => dd("linuxBad", ret = false)
//      case macOSBT(_) => dd("bluetooth", ret = false)
      case raspberryPi(_) => dd("raspberryPi", ret = true) // todo perhaps any Linux
      case macOS(_) => dd("macOS", true) // MacOS
      case d => dd("default", !(d.startsWith("ttyp") || d.contains("Bluetooth")))
    }
  }

}