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

package org.wa9nnn.fdcluster.tools

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfo, RunningTaskInfoConsumer}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.Qso
import play.api.libs.json.Json

class RandomQsoSpec extends Specification {

  "RandomQso" should {
   val rtc =  new RunningTaskInfoConsumer {
      override def update(info: RunningTaskInfo): Unit ={}

      override def done(): Unit = {}
    }

    val randomQso = new RandomQso(rtc)
    "nextQso" in {
      val b = Array.newBuilder[Qso]
      for (_ <-  0 to 10) {
         randomQso.apply(GenerateRandomQsos()){qso =>
          b += qso
        }
        val jqso = Json.toJson(b.result().head)
        println(jqso)
      }
      ok
    }
  }
}
