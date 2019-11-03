
package org.wa9nnn.fdcluster.javafx.cluster

import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import org.wa9nnn.util.CssClassProvider

/**
 * Used to display digest in cluser table.
 * @param qsoHourDigest to be displayed.
 * @param cssClass how to style cell.
 */
case class DigestValue(qsoHourDigest:QsoHourDigest, cssClass:String) extends CssClassProvider{
  def truncated:String = {
    s"${qsoHourDigest.size}: ${qsoHourDigest.digest.take(10)}..."
  }
  def tooltip:String = s"Qso Count: ${qsoHourDigest.size}\ndigest: ${qsoHourDigest.digest}"
}
