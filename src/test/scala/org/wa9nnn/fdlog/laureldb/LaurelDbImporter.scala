
package org.wa9nnn.fdlog.laureldb

import java.net.InetAddress
import java.nio.file.Paths

import com.typesafe.config.{Config, ConfigFactory}
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.store.{NodeInfoImpl, StoreMapImpl}

import scala.io.Source

object LaurelDbImporter {
  val ourContest = Contest("FD", 2019)
  val nodeInfo = new NodeInfoImpl(ourContest, nodeAddress = NodeAddress(0, InetAddress.getLocalHost.toString ))
  val currentStationProvider = new CurrentStationProviderImpl(ConfigFactory.load)
  val store = new StoreMapImpl(nodeInfo, currentStationProvider, null, journalFilePath = Some(Paths.get("/Users/dlieber/fdlog/journal1.log"))) //todo

  println(s"Writing to ${store.journalFilePath}")
  val exchange = Exchange("3A", "IL")

  def main(args: Array[String]): Unit = {

    val inputStream = getClass.getResourceAsStream("/HD.csv")
    val bufferedSource = Source.fromInputStream(inputStream)
    var count = 0
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      val callsign = cols(1)
      val qso = Qso(
        callsign = callsign,
        bandMode = currentStationProvider.currentStation.bandMode,
        exchange = exchange)
      store.add(qso)
      count = count + 1

    }
    bufferedSource.close
    println(s"Wrote $count qsos")

  }

}
