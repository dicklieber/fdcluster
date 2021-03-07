package org.wa9nnn.fdcluster.rig

import com.typesafe.scalalogging.LazyLogging
import ch.qos.logback.classic.{Level, Logger}
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification
import org.wa9nnn.util.StructuredLogging

class SerialPortWrapperSpec extends Specification with DataTables with StructuredLogging {
  setLevel(Level.TRACE)

  "SerialPortWrapper" >> {

    "isUseFull" >> {
      "name" || "useFull" |
        "COM3" !! true |
        "ttyUSB0" !! true |
        "tty.Bluetooth-Incoming-Port" !! false |
        "cu.Bluetooth-Incoming-Port" !! false |
        "tty.usbserial-AI02C62V" !! true |
        "ttyp0" !! false |> { (in, usefull) =>
        MockNamedSerialPort(in).useful must beEqualTo(usefull)
      }
    }

    "other methods" >> {
      val namedSerialPort1 = MockNamedSerialPort("tty.usbserial-CI02C62V")
      "toString" >> {
        namedSerialPort1.toString must beEqualTo("tty.usbserial-CI02C62V (crap)")
      }
      "order" >> {
        val namedSerialPort2 = MockNamedSerialPort("tty.usbserial-B143230", "B143230")

        val list = Seq(namedSerialPort1, namedSerialPort2)
        list.head must be(namedSerialPort1)
        list.sorted.head must be(namedSerialPort2)
      }

    }

  }
}

case class MockNamedSerialPort(name: String, description: String = "crap") extends NamedSerialPort[MockNamedSerialPort] {
}