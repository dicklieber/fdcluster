
package org.wa9nnn.util


import org.wa9nnn.fdcluster.adif
import org.wa9nnn.fdcluster.adif.{AdifCollector, AdifFile, AdifQsoAdapter}
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.store.Store

import java.nio.file.Paths
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
    val adifQsos: Seq[adif.Qso] = adifFile.records

    totalIterations = adifQsos.size

    adifQsos.foreach { adifQso =>
      val qso = AdifQsoAdapter(adifQso)
      store.add(qso)
      addOne()
    }

    done()
  }
}


