package org.wa9nnn.fdcluster.store

import akka.util.ByteString
import com.google.inject.name.Named
import nl.grons.metrics4.scala.{DefaultInstrumented, Timer}
import org.apache.commons.codec.binary.{Base64InputStream, Base64OutputStream}
import org.wa9nnn.fdcluster.model.MessageFormats.Uuid
import org.wa9nnn.fdcluster.model.{NodeAddress, QsoMetadata, QsoRecord}
import play.api.libs.json.Json
import _root_.scalafx.beans.property.ObjectProperty
import _root_.scalafx.collections.ObservableBuffer
import _root_.scalafx.Includes._
import org.wa9nnn.fdcluster.model.MessageFormats._

import java.io.{BufferedReader, ByteArrayInputStream, ByteArrayOutputStream, InputStreamReader, StringWriter}
import java.nio.ByteBuffer
import java.util.UUID
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import javax.inject.{Inject, Singleton}

@Singleton
class SquozeQsos @Inject()(nodeAddress: NodeAddress, @Named("allQsos") allQsos: ObservableBuffer[QsoRecord])
  extends DefaultInstrumented {

  private val uuidEncode: Timer = metrics.timer("uuidEncode")
  private val qsoRecordEncode: Timer = metrics.timer("qsoRecordEncode")


  def encodeUuids(): NodeUuids = {
    uuidEncode.time {
      //      val baos = new ByteArrayOutputStream()
      //      val b64os: Base64OutputStream = new Base64OutputStream(baos)
      //      val os = new GZIPOutputStream(b64os)
      //      allQsos.foreach(qr => {
      //        val uuidUuid = UUID.fromString(qr.qso.uuid)
      //        val bytes = UuidUtils.asBytes(uuidUuid)
      //        os.write(bytes)
      //        os.write(Array('\n'.toByte))
      //      })
      //      os.finish()
      //      os.flush()
      //      os.close()
      val byteBuffer = ByteBuffer.allocate(allQsos.size * 16)
      allQsos.foreach(qr => {
//        val uuid = UUID.fromString(qr.qso.uuid)
        val uuid = qr.qso.uuid
        byteBuffer.putLong(uuid.getLeastSignificantBits)
        byteBuffer.putLong(uuid.getMostSignificantBits)
      })

      val bytes1 = byteBuffer.array()



      val baos = new ByteArrayOutputStream()
      val b64os: Base64OutputStream = new Base64OutputStream(baos)
      val os = new GZIPOutputStream(b64os)
      os.write(bytes1)

      os.finish()
      os.flush()
      os.close()

      NodeUuids(nodeAddress, baos.toString)
    }
  }

  def encodeQsos(): NodeUuids = {
    uuidEncode.time {
      val baos = new ByteArrayOutputStream()
      val b64os: Base64OutputStream = new Base64OutputStream(baos)
      val os = new GZIPOutputStream(b64os)
      allQsos.foreach(qr => {
        val str: String = Json.toJson(qr).toString()
        os.write(str.getBytes)
        os.write(Array('\n'.toByte))
      })
      os.finish()
      os.flush()
      os.close()

      NodeUuids(nodeAddress, baos.toString)
    }
  }

  def decodeUuid(nodeQsos: NodeUuids): IterableOnce[Uuid] = {
    throw new NotImplementedError() //todo
  }


}

class QsoDecoder(nodeQsos: NodeUuids) extends Iterator[QsoRecord] {

  val gZIPInputStream = new GZIPInputStream(new Base64InputStream(new ByteArrayInputStream(nodeQsos.blob.getBytes)))
  val reader = new BufferedReader(new InputStreamReader(gZIPInputStream))

  override def hasNext: Boolean = {
    reader.ready()
  }

  override def next(): QsoRecord = {
    val str = reader.readLine()
    QsoRecord(str)
  }
}

case class NodeUuids(nodeInfo: NodeAddress, blob: String)
