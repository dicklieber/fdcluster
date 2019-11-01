
package org.wa9nnn.fdcluster.javafx.cluster

import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import org.wa9nnn.util.CssClassProvider

case class DigestValue(qhd:QsoHourDigest, cssClass:String) extends CssClassProvider{
  def truncated:String = {
    s"${qhd.size}: ${qhd.digest.take(10)}..."
  }
  def tooltip:String = s"Qso Count: ${qhd.size}\ndigest: ${qhd.digest}"
}
