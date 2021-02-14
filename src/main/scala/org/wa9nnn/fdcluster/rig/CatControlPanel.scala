
package org.wa9nnn.fdcluster.rig

import org.wa9nnn.fdcluster.rig.SerialPortSettings.{baudRates, defautBaudRate}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.GridPane

class CatControlPanel() extends GridPane {
  def setValue(sps: SerialPortSettings): Unit = {
    portComboBox.setValue(sps.port)
    baudRateComboBox.setValue(sps.baudrate)
  }

  private val serialPortNames: Seq[String] = Serial.ports.map(_.name)
  val portComboBox = new ComboBox[String](ObservableBuffer[String](serialPortNames))
  val baudRateComboBox = new ComboBox[String](ObservableBuffer[String](baudRates))
  baudRateComboBox.setValue(defautBaudRate)

  //  val gridPane: GridPane = new GridPane() {
  hgap = 10
  vgap = 10
  padding = Insets(20, 100, 10, 10)

  add(new Label("Serial Port:"), 0, 0)
  add(portComboBox, 1, 0)

  add(new Label("Baud Rate:"), 0, 1)
  add(baudRateComboBox, 1, 1)


  def result: SerialPortSettings = {
    SerialPortSettings(portComboBox.value.value, baudRateComboBox.value.value)
  }
}

case class SerialPortSettings(port: String, baudrate: String)

object SerialPortSettings {
  val baudRates = Seq("115200", "57600", "38400", "19200", "9600", "4800", "1200")
  val defautBaudRate: String = baudRates(3)

  def apply():SerialPortSettings = SerialPortSettings("-", defautBaudRate)
}