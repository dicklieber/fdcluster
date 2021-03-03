
package org.wa9nnn.fdcluster.adif

import java.time.Duration

case class AdifFile(sourceFile: String, header: Seq[AdifEntry], records: Seq[AdifQso], duration: Duration)

case class AdifQso(entries: Set[AdifEntry]) {
  def contains(that: AdifQso): Boolean = {
    val intersection = that.entries.intersect(this.entries)
    intersection == that.entries
  }


  def toMap: Map[String, String] = entries.map(e =>
    e.tag.toUpperCase -> e.value).toMap
}
