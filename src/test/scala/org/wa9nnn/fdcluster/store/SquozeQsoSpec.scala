package org.wa9nnn.fdcluster.store

import akka.util.ByteString
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{BandMode, Exchange, NodeAddress, Qso, QsoMetadata, QsoRecord}
import org.wa9nnn.fdcluster.tools.SequentialCallsigns
import scalafx.collections.ObservableBuffer

import java.nio.ByteBuffer
import scala.collection.mutable.ArrayBuffer

class SquozeQsoSpec extends Specification {

  "SquozeQsoSpec" >> {
    "encodeUuids" >> {
      val nodeAddress = NodeAddress()
      val callsigns = new SequentialCallsigns()
      val allQsos = ObservableBuffer[QsoRecord](
        List.tabulate(100000) { n =>
          QsoRecord(Qso(callsigns.next(), BandMode(), new Exchange()), QsoMetadata())
        }
      )


      val justRawUuids = allQsos.map(_.qso.uuid).mkString("\n")
      val justRawUuidsLength = justRawUuids.length
      val sq = new SquozeQsos(nodeAddress, allQsos)
      val nodeQsos = sq.encodeUuids()
      val compressedBase64Length = nodeQsos.blob.length

      compressedBase64Length must be lessThan (justRawUuidsLength)

      nodeQsos.nodeInfo must beEqualTo(nodeAddress)


    }
    "encodeQso" >> {
      val nodeAddress = NodeAddress()
      val callsigns = new SequentialCallsigns()
      val qsosBin = new ArrayBuffer[Byte]()
      val allQsos = ObservableBuffer[QsoRecord](
        List.tabulate(100000) { n =>
          val qsoRecord = QsoRecord(Qso(callsigns.next(), BandMode(), new Exchange()), QsoMetadata())
          val byteString = qsoRecord.toByteString
          qsosBin.addAll(byteString.toArray)
          qsosBin.addOne('\n'.toByte)
          qsoRecord
        }
      )
      val sq = new SquozeQsos(nodeAddress, allQsos)


      val justRawQsosBinLength = qsosBin.length
      val nodeQsos = sq.encodeQsos()
      val compressedBase64Length = nodeQsos.blob.length

      compressedBase64Length must be lessThan (justRawQsosBinLength)

      nodeQsos.nodeInfo must beEqualTo(nodeAddress)

      val qsoDecoder = new QsoDecoder(nodeQsos)
      qsoDecoder.next() must beEqualTo (allQsos.head)
    }
  }
}
