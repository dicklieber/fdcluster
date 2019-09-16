
package org.wa9nnn.fdlog.laureldb

import java.net.InetAddress

import org.wa9nnn.fdlog.model.{Contest, CurrentStationProviderImpl, Exchange, NodeAddress, Qso}
import org.wa9nnn.fdlog.store.{NodeInfoImpl, StoreMapImpl}

import scala.io.Source

object LaurelDbImporter {
  val ourContest = Contest("FD", 2019)
  val nodeInfo = new NodeInfoImpl(ourContest, nodeAddress = NodeAddress(0, InetAddress.getLocalHost.toString ))
  val currentStationProvider = new CurrentStationProviderImpl()
  val store = new StoreMapImpl(nodeInfo, currentStationProvider, null) //todo

  val exchange = Exchange("3A", "IL")

  def main(args: Array[String]): Unit = {
    val bufferedSource = Source.fromFile("test/org/wa9nnn/fdlog/laureldb/HD.csv")
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      val callsign = cols(1)
      val qso = Qso(
        callsign = callsign,
        bandMode = currentStationProvider.currentStation.bandMode,
        exchange = exchange)
      store.add(qso)

    }
    bufferedSource.close

  }

}
