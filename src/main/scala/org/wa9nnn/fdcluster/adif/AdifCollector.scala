
package org.wa9nnn.fdcluster.adif

import java.net.URL
import java.time.{Duration, Instant}
import scala.io.BufferedSource


object AdifCollector {
  /**
   *
   * @param source in
   * @param url    where this came from
   * @return raw records
   */
  def read(source: BufferedSource, url: Option[URL] = None): AdifFile = {
    val start = Instant.now()
    val entries = List.newBuilder[AdifResult]
    new AdifParser(source)((t: AdifResult) =>
      entries += t
    )

    val r: Seq[AdifResult] = entries.result()
    val (heads: Seq[AdifEntry], qsos) = r.span(_ != AdifResult.eoh)

    val q: Seq[Qso] = qsos
      .tail // pats EOH
      .foldLeft(Seq(Seq.empty[AdifEntry])) {
        ((acc, i) =>
          if (i == AdifResult.eor) acc :+ Seq.empty
          else acc.init :+ (acc.last :+ i.asInstanceOf[AdifEntry])
          )
      }
      .filterNot(_.isEmpty) // get rid of nothing after lasst EOR
      .map(Qso) // put into Qsos

    AdifFile(url.map(_.toExternalForm).getOrElse(""), heads, q, Duration.between(start, Instant.now()))

  }
}