
package org.wa9nnn.fdcluster.rig

class RigIo(socketAdapter: SocketAdapter) extends Rig {

  def get(command: String): Seq[String] = {
    socketAdapter.doOp(command)
  }

  def getInt(command: String): Int = {
    val value = get(command)
    value.map { s ⇒ s.toInt }.head
  }

  override def frequency: Int = {
    getInt("f")
  }

  override def modeAndBandWidth: (String, Int) = {
    val lines = get("m")
    (lines.head, lines(1).toInt)
  }

  override def radio: String = ""

  override def caps: Map[String, String] = {
    (for {
      cap <- get("1")
    } yield {
      val strings: Array[String] = cap.split(",").map(_.trim)
      val length = strings.length
      val rValue = if(length > 1) strings(1) else ""
      (strings.head, rValue)
    }).toMap
  }
}

object RigIo {
  val defaultPort = 4532

  def apply(hostAndPort: String): RigIo = {
    new RigIo(SocketAdapter(hostAndPort, defaultPort))
  }

  def main(args: Array[String]): Unit = {
    val rigIo = RigIo("192.168.0.177")
    println(rigIo.frequency)
    println(rigIo.modeAndBandWidth)

  }
}