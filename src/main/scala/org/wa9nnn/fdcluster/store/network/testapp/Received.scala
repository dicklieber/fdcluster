package org.wa9nnn.fdcluster.store.network.testapp

import scalafx.beans.property.{IntegerProperty, ObjectProperty, ReadOnlyIntegerProperty, ReadOnlyIntegerWrapper, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{TableColumn, TableView, TitledPane}

import java.net.{DatagramPacket, InetAddress}
import java.time.LocalTime
import java.time.format.{DateTimeFormatter, FormatStyle}

abstract class  Received(recv: DatagramPacket, _kind:String) {
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)

  val source: InetAddress = recv.getAddress
  val message: StringProperty = StringProperty(new String(recv.getData, 0, recv.getLength))
  val time: StringProperty = StringProperty(dateTimeFormatter.format(LocalTime.now()))
  val osName: StringProperty = StringProperty(s"${System.getProperty("os.name")}")
  val kind: StringProperty = StringProperty(_kind.take(1))

  val length: IntegerProperty =  IntegerProperty(recv.getLength)
}


class HostMessages(val source: InetAddress) extends TitledPane {
  prefWidth = 400.0
  text = source.getCanonicalHostName

  private val buffer = new FixedObservableBuffer[Received](5)

  private val tableView: TableView[Received] = new TableView[Received]() {
    prefWidth = 300.0
    items = buffer
    columns ++= List(
      new TableColumn[Received, String] {
        text = "Stamp"
        cellValueFactory = { value => value.value.time }
      },
        new TableColumn[Received, String] {
        text = "Os"
        cellValueFactory = { value => value.value.osName }
      },
      new TableColumn[Received, String] {
        text = "B/M"
        cellValueFactory = { value => value.value.kind }
      },
      new TableColumn[Received, Int] {
        text = "Length"
        cellValueFactory = { value =>  {
          ObjectProperty[Int](value.value.message.value.length)
        }}
      },
      new TableColumn[Received, String] {
        text = "Message"
        prefWidth = 150
        cellValueFactory = { value => value.value.message }
      }
    )
  }
  content = tableView

  def apply(received: Received): Unit = {
    buffer.prepend(received)
  }

}


class FixedObservableBuffer[T](max: Int) extends ObservableBuffer[T] {
  override def prepend(elem: T): FixedObservableBuffer.this.type = {
    if (size >= max) {
      remove(max - 1)
    }
    super.prepend(elem)
  }
}