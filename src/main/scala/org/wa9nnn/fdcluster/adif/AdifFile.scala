
package org.wa9nnn.fdcluster.adif

import java.time.Duration

case class AdifFile(sourceFile:String, header: Seq[AdifEntry], records: Seq[Qso], duration: Duration)

case class Qso(entries:Seq[AdifEntry]) {
  def toMap:Map[String, String] = entries.map(e =>
    e.tag.toUpperCase -> e.value).toMap
}
