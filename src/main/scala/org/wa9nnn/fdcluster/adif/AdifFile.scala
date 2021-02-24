
package org.wa9nnn.fdcluster.adif

import java.time.Duration

case class AdifFile(sourceFile:String, header: Seq[AdifEntry], records: Seq[Qso], duration: Duration)

case class Qso(entries:Seq[AdifEntry])
