
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.adif

import java.net.URL
import scala.io.Source


object AdifCollector {
  /**
   *
   * @param source in
   * @param url    where this came from
   * @return raw records
   */
  def read(source: Source, url: Option[URL] = None): AdifFile = {
    val entries = List.newBuilder[AdifResult]
    new AdifParser(source)((t: AdifResult) =>
      entries += t
    )

    val r: Seq[AdifResult] = entries.result()
    val (heads, qsos) = r.span(_ != AdifResult.eoh)

    val adifQsos: Seq[AdifQso] = qsos
      .tail // past EOH
      .foldLeft(Seq(Seq.empty[AdifEntry])) {
        (acc, i) =>
          if (i == AdifResult.eor) acc :+ Seq.empty
          else acc.init :+ (acc.last :+ i.asInstanceOf[AdifEntry])
      }
      .filterNot(_.isEmpty) // get rid of nothing after last EOR
      .map(e => AdifQso(e.toSet)) // put into Qsos
    AdifFile(heads.asInstanceOf[Seq[AdifEntry]], adifQsos)

  }
}