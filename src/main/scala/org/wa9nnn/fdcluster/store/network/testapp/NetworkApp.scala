package org.wa9nnn.fdcluster.store.network.testapp

import com.typesafe.scalalogging.LazyLogging
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.javafx.GridOfControls
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.{Label, Slider}
import scalafx.scene.layout.{HBox, TilePane, VBox}

import java.io.IOException
import java.net.{DatagramPacket, DatagramSocket, InetAddress, MulticastSocket}
import java.util.{Timer, TimerTask}
import scala.collection.concurrent.TrieMap

object NetworkApp extends JFXApp3 with LazyLogging {
  override def start(): Unit = {
    val localHost = InetAddress.getLocalHost
    val multicastAddress = InetAddress.getByName("239.73.88.0")
    logger.info(s"NetworkApp on ${localHost.getHostName}")

    val hosts = new TrieMap[InetAddress, HostMessages]()

    val tilePane = new TilePane()

    val slider = new Slider() {
      value = 100.0
      max = 5000.0
    }
    val lengthDisplay = new Label()
    slider.value.onChange { (_, _, nv: Number) =>
      lengthDisplay.text = f"${nv.intValue()}"
    }
    val goc = new GridOfControls()

    stage = new PrimaryStage {
      scene = new Scene {
        content = new VBox(

          new HBox(goc),
          tilePane
        )
      }
    }


    def add(received: Received): Unit = {
      onFX {
        var newHost = false
        val maybeMessages: HostMessages = hosts.getOrElseUpdate(received.source, {
          newHost = true
          new HostMessages(received.source)
        })
        maybeMessages.apply(received)
        if (newHost) {
          tilePane.children = ObservableBuffer.from(hosts.values)
        }
      }

    }

    val multicast = new Multicast(multicastAddress, 1501)((r: Received) =>
      add(r)
    )
    val broadcast = new Broadcast(1502)((r: Received) =>
      add(r)
    )
    val timer = new Timer("PropertyCellTimer", true)
    var sn = 0
    timer.scheduleAtFixedRate(new TimerTask {
      override def run(): Unit = {
        val length = slider.value.toInt
        val message: String = s"$sn:$length ${"%" * length}".take(length)
        multicast.send(message)
        broadcast.send(message)
        sn += 1
      }
    }, 10, 3000)
    goc.addControl("Length", slider, lengthDisplay)
    goc.addControl("Multicast Error", multicast.errControl)
    goc.addControl("Broadcast Error", broadcast.errControl)
  }


  class Multicast(multicastGroup: InetAddress, port: Int)(callback: Received => Unit) extends LazyLogging {
    var multicastSocket: MulticastSocket = new MulticastSocket(port)
    multicastSocket.setReuseAddress(true)
    multicastSocket.joinGroup(multicastGroup)

    var buf = new Array[Byte](10000)

    new Thread(() => {
      logger.info(s"Listening on ${multicastGroup.getHostName}:$port")
      do {
        val recv: DatagramPacket = new DatagramPacket(buf, buf.length)
        multicastSocket.receive(recv)
        callback(MulticastReceived(recv))
      } while (true)

    }).start()

    val errControl = new Label()

    def send(message: String): Unit = {
      try {
        multicastSocket.send(new DatagramPacket(message.getBytes, message.length, multicastGroup, port))
        onFX {
          errControl.text = ""}
      } catch {
        case eio: IOException =>
          onFX {
            errControl.text = eio.getMessage
          }
      }
    }

    case class MulticastReceived(packet: DatagramPacket) extends Received(packet, "Multicast")
  }

  class Broadcast(port: Int)(data: Received => Unit) extends LazyLogging {
    val address: InetAddress = InetAddress.getByName("255.255.255.255")
    val socket = new DatagramSocket(port)

    var buf = new Array[Byte](10000)

    new Thread(() => {
      logger.info(s"Listening on ${address.getHostName}:$port")
      do {
        val recv: DatagramPacket = new DatagramPacket(buf, buf.length)
        socket.receive(recv)
        val bytes: Array[Byte] = recv.getData
        val sData = new String(bytes)
        data(new BroadcastReceived(recv))
      } while (true)

    }).start()


    def send(message: String): Unit = {
      try {
        socket.send(new DatagramPacket(message.getBytes, message.length, address, port))
        onFX {
          errControl.text = ""
        }
      } catch {
        case eio: IOException =>
          onFX {
            errControl.text = eio.getMessage
          }
      }

    }

    class BroadcastReceived(recv: DatagramPacket) extends Received(recv, "Broadcast")

    val errControl = new Label()
  }
}