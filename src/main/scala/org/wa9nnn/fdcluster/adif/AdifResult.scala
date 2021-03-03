
package org.wa9nnn.fdcluster.adif

import java.io.PrintWriter

/**
 * Things the the [[AdifParser]] send to callback.
 */
sealed trait AdifResult {
  def toLine:String

}

object AdifResult {
  val eoh: AdifSeperator = AdifSeperator("EOH")
  val eor: AdifSeperator = AdifSeperator("EOR")
}

case class AdifEntry(tag: String, value: String) extends AdifResult with Ordered[AdifEntry] {
  assert(tag == tag.toUpperCase, s"tag must be all caps! got:$tag")

  def toLine: String = s"<$tag:${value.length}>$value\r\n"

  override def compare(that: AdifEntry): Int = {

    this.tag compareTo(that.tag)
  }
}

case class AdifSeperator(name: String) extends AdifResult {
  override def equals(obj: Any): Boolean = {
    obj match {
      case AdifSeperator(n) =>
        name equalsIgnoreCase (n)
      case _ => false
    }

  }

  override val toLine: String = s"<$name>"
}

