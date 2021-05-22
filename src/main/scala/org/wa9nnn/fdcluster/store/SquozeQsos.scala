package org.wa9nnn.fdcluster.store

import _root_.scalafx.collections.ObservableBuffer
import com.google.inject.name.Named
import nl.grons.metrics4.scala.{DefaultInstrumented, Timer}
import org.apache.commons.codec.binary.{Base64InputStream, Base64OutputStream}
import org.wa9nnn.fdcluster.model.MessageFormats.{Uuid, _}
import org.wa9nnn.fdcluster.model.{NodeAddress, Qso}
import play.api.libs.json.Json

import java.io.{BufferedReader, ByteArrayInputStream, ByteArrayOutputStream, InputStreamReader}
import java.nio.ByteBuffer
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import javax.inject.{Inject, Singleton}

@Singleton
class SquozeQsos @Inject()(nodeAddress: NodeAddress, @Named("allQsos") allQsos: ObservableBuffer[Qso])
  extends DefaultInstrumented {

  private val uuidEncode: Timer = metrics.timer("uuidEncode")
  private val QsoEncode: Timer = metrics.timer("QsoEncode")


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
        val uuid = qr.uuid
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

class QsoDecoder(nodeQsos: NodeUuids) extends Iterator[Qso] {

  val gZIPInputStream = new GZIPInputStream(new Base64InputStream(new ByteArrayInputStream(nodeQsos.blob.getBytes)))
  val reader = new BufferedReader(new InputStreamReader(gZIPInputStream))

  override def hasNext: Boolean = {
    reader.ready()
  }

  override def next(): Qso = {
    val str = reader.readLine()
    Qso(str)
  }
}

case class NodeUuids(nodeInfo: NodeAddress, blob: String)
