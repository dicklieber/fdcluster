
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

package org.wa9nnn.util

import org.wa9nnn.fdcluster.adif
import org.wa9nnn.fdcluster.adif.{AdifCollector, AdifFile, AdifQsoAdapter}
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.store.Store

import javax.inject.Inject
import scala.io.Source

/**
 * Only invoke from[[org.wa9nnn.fdcluster.store.StoreActor]]
 *
 * @param store where to put
 * @param runningTaskInfoConsumer progress UI
 */
class ImportTask @Inject()(store: Store, val runningTaskInfoConsumer: RunningTaskInfoConsumer) extends RunningTask {
  override val taskName: String = "Import"

  def apply(sPath: String) {
    //todo cabrillo starts with: START-OF-LOG
    val adifFile: AdifFile = AdifCollector.read(Source.fromFile(sPath))
    val adifQsos: Seq[adif.AdifQso] = adifFile.records

    totalIterations = adifQsos.size

    adifQsos.foreach { adifQso =>
      val qso = AdifQsoAdapter(adifQso)
      store.add(qso)
      addOne()
    }

    done()
  }
}


