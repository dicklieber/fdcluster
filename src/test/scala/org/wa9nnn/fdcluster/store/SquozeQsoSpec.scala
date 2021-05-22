package org.wa9nnn.fdcluster.store

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.tools.SequentialCallsigns
import _root_.scalafx.collections.ObservableBuffer

import scala.collection.mutable.ArrayBuffer

class SquozeQsoSpec extends Specification {

  "SquozeQsoSpec" >> {
    "encodeUuids" >> {
      val nodeAddress = NodeAddress()
      val callSigns = new SequentialCallsigns()
      val allQsos = ObservableBuffer.from(
        List.tabulate(100000) { _ =>
          Qso(callSigns.next(), Exchange(), BandMode(), QsoMetadata())
        }
      )


      val justRawUuids = allQsos.map(_.uuid).mkString("\n")
      val justRawUuidsLength = justRawUuids.length
      val sq = new SquozeQsos(nodeAddress, allQsos)
      val nodeQsos = sq.encodeUuids()
      val compressedBase64Length = nodeQsos.blob.length

      compressedBase64Length must be lessThan justRawUuidsLength

      nodeQsos.nodeInfo must beEqualTo(nodeAddress)


    }
    "encodeQso" >> {
      val nodeAddress = NodeAddress()
      val callSigns = new SequentialCallsigns()
      val qsosBin = new ArrayBuffer[Byte]()
      val allQsos: ObservableBuffer[Qso] = ObservableBuffer.from(
        List.tabulate(100000) { _ =>
          val qsoRecord = Qso(callSigns.next(), Exchange(), BandMode(), QsoMetadata())
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

      compressedBase64Length must be lessThan justRawQsosBinLength

      nodeQsos.nodeInfo must beEqualTo(nodeAddress)

      val qsoDecoder = new QsoDecoder(nodeQsos)
      qsoDecoder.next() must beEqualTo(allQsos.head)
    }
  }
}
