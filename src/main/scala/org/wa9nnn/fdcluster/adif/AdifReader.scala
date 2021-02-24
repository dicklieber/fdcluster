
package org.wa9nnn.fdcluster.adif

import scala.io.BufferedSource


object AdifReader {
  /**
   *
   * @param source in
   * @return raw records
   */
  def read(source: BufferedSource): List[Entry] = {
    val entries = List.newBuilder[Entry]
    val fc = new FieldCollector((t: Entry) =>
      entries += t
    )
    source.foreach { ch: Char =>
      fc(ch)
    }
    entries.result()
  }
}